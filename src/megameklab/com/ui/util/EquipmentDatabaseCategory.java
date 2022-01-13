/*
 * MegaMekLab
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
 *
 * This program is  free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package megameklab.com.ui.util;

import megamek.common.*;
import megamek.common.weapons.tag.TAGWeapon;
import megameklab.com.util.UnitUtil;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import static megamek.common.WeaponType.*;
import static megamek.common.MiscType.*;

/**
 * Equipment categories used for filtering the equipment database and deciding which filters to show.
 *
 * @author Neoancient
 * @author Simon (Juliez) (Additions)
 */
public enum EquipmentDatabaseCategory {

    ENERGY ("Energy",
            (eq, en) -> (eq instanceof WeaponType) && !((WeaponType) eq).isCapital()
            && (eq.hasFlag(F_ENERGY)
            || ((eq.hasFlag(F_PLASMA) && (((WeaponType) eq).getAmmoType() == AmmoType.T_PLASMA))))),

    BALLISTIC ("Ballistic",
            (eq, en) -> (eq instanceof WeaponType) && !((WeaponType) eq).isCapital() && eq.hasFlag(F_BALLISTIC)),

    MISSILE ("Missile",
            (eq, en) -> (eq instanceof WeaponType) && !((WeaponType) eq).isCapital() && eq.hasFlag(F_MISSILE)),

    ARTILLERY ("Artillery",
            (eq, en) -> (eq instanceof WeaponType) && eq.hasFlag(F_ARTILLERY),
            e -> !(e instanceof Protomech)
                    && (!(e instanceof Infantry) || (e instanceof BattleArmor))),

    CAPITAL ("Capital",
            (eq, en) -> (eq instanceof WeaponType) && ((WeaponType) eq).isCapital(),
            Entity::isLargeCraft),

    PHYSICAL ("Physical",
            (eq, en) -> UnitUtil.isPhysicalWeapon(eq),
            e -> e.hasETypeFlag(Entity.ETYPE_MECH)),

    AMMO ("Ammo",
            (eq, en) -> (eq instanceof AmmoType) && !(eq instanceof BombType)
            && UnitUtil.canUseAmmo(en, (AmmoType) eq, false),
            e -> e.getWeightClass() != EntityWeightClass.WEIGHT_SMALL_SUPPORT),

    OTHER ("Other",
            (eq, en) -> ((eq instanceof MiscType)
            && !UnitUtil.isPhysicalWeapon(eq)
            && !UnitUtil.isJumpJet(eq)
            && !UnitUtil.isHeatSink(eq)
            && !eq.hasFlag(F_TSM)
            && !eq.hasFlag(F_INDUSTRIAL_TSM)
            && !(eq.hasFlag(F_MASC)
            && !eq.hasSubType(S_SUPERCHARGER)
            && !eq.hasSubType(S_JETBOOSTER))
            && !(en.hasETypeFlag(Entity.ETYPE_QUADVEE) && eq.hasFlag(F_TRACKS))
            && !UnitUtil.isArmorOrStructure(eq)
            && !eq.hasFlag(F_CHASSIS_MODIFICATION)
            && !(en.isSupportVehicle() && (eq.hasFlag(F_BASIC_FIRECONTROL) || (eq.hasFlag(F_ADVANCED_FIRECONTROL))))
            && !eq.hasFlag(F_MAGNETIC_CLAMP)
            && !(eq.hasFlag(F_PARTIAL_WING) && en.hasETypeFlag(Entity.ETYPE_PROTOMECH)))
            && !eq.hasFlag(F_SPONSON_TURRET)
            && !eq.hasFlag(F_PINTLE_TURRET)
            || (eq instanceof TAGWeapon)),

    AP ("Anti-Personnel",
            (eq, en) -> UnitUtil.isBattleArmorAPWeapon(eq),
            e -> e instanceof BattleArmor),

    PROTOTYPE ("Prototype",
            (eq, en) -> (eq instanceof WeaponType) && eq.hasFlag(WeaponType.F_PROTOTYPE),
            e -> !(e instanceof BattleArmor)),

    ONE_SHOT ("One-Shot",
            (eq, en) -> (eq instanceof WeaponType) && eq.hasFlag(WeaponType.F_ONESHOT)),

    TORPEDO ("Torpedoes",
            (eq, en) -> (eq instanceof WeaponType)
                    && (((WeaponType) eq).getAmmoType() == AmmoType.T_LRM_TORPEDO
                    || ((WeaponType) eq).getAmmoType() == AmmoType.T_SRM_TORPEDO),
            e -> !(e instanceof BattleArmor) && !(e instanceof Aero)),

    UNAVAILABLE ("Unavailable")
            // TODO: Provide MM.ITechManager.isLegal in static form
    ;

    private final static Set<EquipmentDatabaseCategory> showFilters = EnumSet.of(ENERGY, BALLISTIC, MISSILE,
            ARTILLERY, CAPITAL, PHYSICAL, AMMO, OTHER);

    private final static Set<EquipmentDatabaseCategory> hideFilters = EnumSet.of(PROTOTYPE, AP,
            ONE_SHOT, UNAVAILABLE);

    private final String displayName;
    private final BiFunction<EquipmentType, Entity, Boolean> filter;
    private final Function<Entity, Boolean> showForEntity;

    EquipmentDatabaseCategory(String displayName) {
        this(displayName, (eq, en) -> true, e -> true);
    }

    EquipmentDatabaseCategory(String displayName, BiFunction<EquipmentType, Entity, Boolean> filter) {
        this(displayName, filter, e -> true);
    }

    EquipmentDatabaseCategory(String displayName,
                              BiFunction<EquipmentType, Entity, Boolean> filter,
                              Function<Entity, Boolean> showForEntity) {
        this.displayName = displayName;
        this.filter = filter;
        this.showForEntity = showForEntity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean showFilterFor(Entity en) {
        return showForEntity.apply(en);
    }

    public boolean passesFilter(EquipmentType eq, Entity en) {
        return filter.apply(eq, en);
    }

    /** Returns a Set of the filters that should act as "Show..." filters. */
    public static Set<EquipmentDatabaseCategory> getShowFilters() {
        return Collections.unmodifiableSet(showFilters);
    }

    /** Returns a Set of the filters that should act as "Hide..." filters. */
    public static Set<EquipmentDatabaseCategory> getHideFilters() {
        return Collections.unmodifiableSet(hideFilters);
    }
}
