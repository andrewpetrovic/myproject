package com.itic.mobile.util.database;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;

/**
 * 
 * @author andrew
 *
 */
public class SQLiteTable {
    String mTableName;

    String createUniqueIndexColumn;

    ArrayList<Column> mColumnsDefinitions = new ArrayList<Column>();

    public String getTableName() {
        return mTableName;
    }

    /**
     * 会自动添加主键 BaseColumns._ID
     *
     * @param tableName
     */
    public SQLiteTable(String tableName) {
        mTableName = tableName;
        mColumnsDefinitions.add(new Column(BaseColumns._ID, Column.Constraint.PRIMARY_KEY,
                Column.DataType.INTEGER));
    }

    public SQLiteTable addColumn(Column columnsDefinition) {
        mColumnsDefinitions.add(columnsDefinition);
        return this;
    }

    public SQLiteTable addColumn(String columnName, Column.DataType dataType) {
        mColumnsDefinitions.add(new Column(columnName, null, dataType));
        return this;
    }

    public SQLiteTable addColumn(String columnName, Column.Constraint constraint,
                                 Column.DataType dataType) {
        mColumnsDefinitions.add(new Column(columnName, constraint, dataType));
        return this;
    }

    /**
     * 为table设置唯一索引
     * @param columnName 用作唯一索引的字段
     */
    public SQLiteTable createUniqueIndex(String columnName){
        createUniqueIndexColumn = columnName;
        return this;
    }

    public void create(SQLiteDatabase db) {
        //拼接创建表SQL语句
        String formatter = " %s";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CREATE TABLE IF NOT EXISTS ");
        stringBuilder.append(mTableName);
        stringBuilder.append("(");
        int columnCount = mColumnsDefinitions.size();
        int index = 0;
        for (Column columnsDefinition : mColumnsDefinitions) {
            stringBuilder.append(columnsDefinition.getColumnName()).append(
                    String.format(formatter, columnsDefinition.getDataType().name()));
            Column.Constraint constraint = columnsDefinition.getConstraint();

            if (constraint != null) {
                stringBuilder.append(String.format(formatter, constraint.toString()));
            }
            if (index < columnCount - 1) {
                stringBuilder.append(",");
            }
            index++;
        }
        stringBuilder.append(");");
        //执行创建表
        try {
            db.beginTransaction();
        db.execSQL(stringBuilder.toString());
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            db.endTransaction();
        }
        //如果需要创建唯一索引，拼接创建唯一索引语句
        if (createUniqueIndexColumn != null){
            if (!createUniqueIndexColumn.equals("")){
                //清空stringbuffer
                stringBuilder.delete(0,stringBuilder.length());
                //拼接sql语句
                stringBuilder.append("CREATE UNIQUE INDEX unique_index_");
                stringBuilder.append(createUniqueIndexColumn + " ");
                stringBuilder.append("ON ");
                stringBuilder.append(mTableName + " ");
                stringBuilder.append("(");
                stringBuilder.append(createUniqueIndexColumn);
                stringBuilder.append(");");
                String sql = stringBuilder.toString();
                System.out.println(sql);
                try {
                    db.beginTransaction();
                    db.execSQL(sql);
                    db.setTransactionSuccessful();
                } catch (SQLException e) {
                    e.printStackTrace();
                }finally{
                    db.endTransaction();
                }
            }
        }
        stringBuilder.delete(0,stringBuilder.length());
    }

    public void delete(final SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + mTableName);
    }
}
