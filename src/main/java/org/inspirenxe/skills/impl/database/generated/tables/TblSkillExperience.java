/*
 * This file is generated by jOOQ.
*/
package org.inspirenxe.skills.impl.database.generated.tables;


import org.inspirenxe.skills.impl.database.generated.DefaultSchema;
import org.inspirenxe.skills.impl.database.generated.Keys;
import org.inspirenxe.skills.impl.database.generated.tables.records.TblSkillExperienceRecord;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Generated;


/**
 * This class is generated by jOOQ.
 */
@Generated(
        value = {
                "http://www.jooq.org",
                "jOOQ version:3.9.4"
        },
        comments = "This class is generated by jOOQ"
)
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class TblSkillExperience extends TableImpl<TblSkillExperienceRecord> {

    /**
     * The reference instance of <code>tbl_skill_experience</code>
     */
    public static final TblSkillExperience TBL_SKILL_EXPERIENCE = new TblSkillExperience();
    private static final long serialVersionUID = -658347773;
    /**
     * The column <code>tbl_skill_experience.id</code>.
     */
    public final TableField<TblSkillExperienceRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this, "");
    /**
     * The column <code>tbl_skill_experience.created</code>.
     */
    public final TableField<TblSkillExperienceRecord, Timestamp> CREATED = createField("created", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false)
            .defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");
    /**
     * The column <code>tbl_skill_experience.last_gained_experience</code>.
     */
    public final TableField<TblSkillExperienceRecord, Timestamp> LAST_GAINED_EXPERIENCE = createField("last_gained_experience",
            org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false)
                    .defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");
    /**
     * The column <code>tbl_skill_experience.container_unique_id</code>.
     */
    public final TableField<TblSkillExperienceRecord, UUID> CONTAINER_UNIQUE_ID =
            createField("container_unique_id", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");
    /**
     * The column <code>tbl_skill_experience.holder_unique_id</code>.
     */
    public final TableField<TblSkillExperienceRecord, UUID> HOLDER_UNIQUE_ID =
            createField("holder_unique_id", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");
    /**
     * The column <code>tbl_skill_experience.fk_skill_type</code>.
     */
    public final TableField<TblSkillExperienceRecord, String> FK_SKILL_TYPE =
            createField("fk_skill_type", org.jooq.impl.SQLDataType.VARCHAR.length(100).nullable(false), this, "");
    /**
     * The column <code>tbl_skill_experience.experience</code>.
     */
    public final TableField<TblSkillExperienceRecord, BigDecimal> EXPERIENCE = createField("experience",
            org.jooq.impl.SQLDataType.NUMERIC.nullable(false).defaultValue(org.jooq.impl.DSL.field("0", org.jooq.impl.SQLDataType.NUMERIC)), this,
            "");

    /**
     * Create a <code>tbl_skill_experience</code> table reference
     */
    public TblSkillExperience() {
        this("tbl_skill_experience", null);
    }

    /**
     * Create an aliased <code>tbl_skill_experience</code> table reference
     */
    public TblSkillExperience(String alias) {
        this(alias, TBL_SKILL_EXPERIENCE);
    }

    private TblSkillExperience(String alias, Table<TblSkillExperienceRecord> aliased) {
        this(alias, aliased, null);
    }

    private TblSkillExperience(String alias, Table<TblSkillExperienceRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TblSkillExperienceRecord> getRecordType() {
        return TblSkillExperienceRecord.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<TblSkillExperienceRecord, Integer> getIdentity() {
        return Keys.IDENTITY_TBL_SKILL_EXPERIENCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<TblSkillExperienceRecord> getPrimaryKey() {
        return Keys.PK_TBL_SKILL_EXPERIENCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<TblSkillExperienceRecord>> getKeys() {
        return Arrays.<UniqueKey<TblSkillExperienceRecord>>asList(Keys.PK_TBL_SKILL_EXPERIENCE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<TblSkillExperienceRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<TblSkillExperienceRecord, ?>>asList(Keys.FK_TBL_SKILL_EXPERIENCE_TBL_SKILL_TYPE_1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TblSkillExperience as(String alias) {
        return new TblSkillExperience(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public TblSkillExperience rename(String name) {
        return new TblSkillExperience(name, null);
    }
}