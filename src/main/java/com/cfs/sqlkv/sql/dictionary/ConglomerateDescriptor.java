package com.cfs.sqlkv.sql.dictionary;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.UniqueTupleDescriptor;
import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.factory.UUIDFactory;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 19:29
 */
public class ConglomerateDescriptor extends UniqueTupleDescriptor {

    private long	conglomerateNumber;
    private String	name;
    private transient String[]	columnNames;
    private final boolean	indexable;
    private final boolean	forConstraint;
    private final IndexRowGenerator	indexRowGenerator;
    private final UUID	uuid;
    private final UUID	tableID;
    private final UUID	schemaID;

    /**
     * Constructor for a conglomerate descriptor.
     *
     * @param dataDictionary		当前描述的数据字典
     * @param conglomerateNumber	The number for the conglomerate
     * @param name			The name of the conglomerate, if any
     * @param indexable		TRUE means the conglomerate is indexable,
     *				FALSE means it isn't
     * @param indexRowGenerator	The descriptor of the index if it's not a
     *							heap
     * @param forConstraint		TRUE means the conglomerate is an index backing up
     *							a constraint, FALSE means it isn't
     * @param uuid		UUID  for this conglomerate
     * @param tableID	UUID for the table that this conglomerate belongs to
     * @param schemaID	UUID for the schema that this conglomerate belongs to
     */
    ConglomerateDescriptor(DataDictionary dataDictionary,
                           long conglomerateNumber,
                           String name,
                           boolean indexable,
                           IndexRowGenerator indexRowGenerator,
                           boolean forConstraint,
                           UUID uuid,
                           UUID tableID,
                           UUID schemaID) {
        super( dataDictionary );
        this.conglomerateNumber = conglomerateNumber;
        this.name = name;
        this.indexable = indexable;
        this.indexRowGenerator = indexRowGenerator;
        this.forConstraint = forConstraint;
        this.uuid = uuid;
        this.tableID = tableID;
        this.schemaID = schemaID;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
