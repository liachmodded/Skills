/*
 * This file is generated by jOOQ.
*/
package org.inspirenxe.skills.impl.database.generated;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.inspirenxe.skills.impl.database.generated.tables.SqliteSequence;
import org.inspirenxe.skills.impl.database.generated.tables.TblSkillExperience;
import org.inspirenxe.skills.impl.database.generated.tables.TblSkillType;
import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.6"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DefaultSchema extends SchemaImpl {

    private static final long serialVersionUID = -232553302;

    /**
     * The reference instance of <code></code>
     */
    public static final DefaultSchema DEFAULT_SCHEMA = new DefaultSchema();

    /**
     * The table <code>sqlite_sequence</code>.
     */
    public final SqliteSequence SQLITE_SEQUENCE = org.inspirenxe.skills.impl.database.generated.tables.SqliteSequence.SQLITE_SEQUENCE;

    /**
     * The table <code>tbl_skill_experience</code>.
     */
    public final TblSkillExperience TBL_SKILL_EXPERIENCE = org.inspirenxe.skills.impl.database.generated.tables.TblSkillExperience.TBL_SKILL_EXPERIENCE;

    /**
     * The table <code>tbl_skill_type</code>.
     */
    public final TblSkillType TBL_SKILL_TYPE = org.inspirenxe.skills.impl.database.generated.tables.TblSkillType.TBL_SKILL_TYPE;

    /**
     * No further instances allowed
     */
    private DefaultSchema() {
        super("", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            SqliteSequence.SQLITE_SEQUENCE,
            TblSkillExperience.TBL_SKILL_EXPERIENCE,
            TblSkillType.TBL_SKILL_TYPE);
    }
}
