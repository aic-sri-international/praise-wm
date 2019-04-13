package com.sri.ai.praisewm.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectSelectStep;
import org.jooq.Table;
import org.jooq.UniqueKey;
import org.jooq.UpdatableRecord;

/** JooqDbUtil - Static utility methods. No methods will commit the transaction. */
public final class JooqDbUtil {
  private static final RecordNonNullFieldFilter UPDATE_NON_NULL_FIELDS_FILTER =
      new RecordNonNullFieldFilter();

  private JooqDbUtil() {}

  /**
   * Get all table entries.
   *
   * @param ctx the JOOQ DSL context
   * @param table the table
   * @param returnType the POJO class type to return
   * @param filter optional field filter
   * @param <T> class of POJO in list
   * @return list of resulting POJOs or an empty list if none
   */
  public static <T> List<T> getAll(
      DSLContext ctx, Table table, Class<T> returnType, FieldFilter filter) {
    SelectSelectStep<Record> step;

    if (filter == null) {
      step = ctx.select();
    } else if (filter instanceof InclusiveFields) {
      step = ctx.select(filter.getFields());
    } else if (filter instanceof ExclusiveFields) {
      step = ctx.select(Filters.filterExcludeByName(table, filter.getFields()));
    } else {
      throw new IllegalArgumentException(
          "Unsupported FieldFilter type: " + filter.getClass().getName());
    }

    return step.from(table).fetch().into(returnType);
  }

  /**
   * Create an {@link ExclusiveFields} filter.
   *
   * @param exclusiveField one or more fields to exclude
   * @return the exclusive fields filter
   */
  public static ExclusiveFields exclusiveFields(Field<?>... exclusiveField) {
    return new ExclusiveFields(exclusiveField);
  }

  /**
   * Create an {@link InclusiveFields} filter.
   *
   * @param inclusiveField one or more fields to include
   * @return the inclusive fields filter
   */
  public static InclusiveFields inclusiveFields(Field<?>... inclusiveField) {
    return new InclusiveFields(inclusiveField);
  }

  /**
   * Insert a new row into the database.
   *
   * <p>Upon a successful return from this method, the new row will not yet have been committed to
   * the database.
   *
   * <p>This method expects the table to have a single column auto-increment primary key represented
   * by either a Java Integer or Long data type.
   *
   * @param dslContext the database connection context
   * @param table the database table to be inserted into
   * @param pojo the POJO that contains the column data to be inserted
   * @return the primary key used for the inserted entry
   * @throws IllegalArgumentException if the table does not contain one and only one primary key
   *     column that is defined as being represented by the Java Integer or Long class type.
   * @throws RuntimeException if an error occurs.
   */
  public static Number insert(DSLContext dslContext, Table table, Object pojo) {
    UpdatableRecord<?> record = createUpdatableRecord(dslContext, table, pojo);
    record.store();
    return (Number) record.key().getValue(0);
  }

  /**
   * Creates a jOOQ UpdatableRecord from a POJO.
   *
   * <p>The table must meet the following constaints
   *
   * <ul>
   *   <li>The table has one and only 1 primary key column
   *   <li>The primary key is a type of Integer or Long
   * </ul>
   *
   * @param dslContext the DSL context
   * @param table the jOOQ table
   * @param pojo the POJO containing the data
   * @return the jOOQ UpdatableRecord
   * @throws IllegalArgumentException if constraints are not met
   */
  private static UpdatableRecord<?> createUpdatableRecord(
      DSLContext dslContext, Table<?> table, Object pojo) {
    validatePrimaryKey(table);
    return toUpdatable(dslContext.newRecord(table, pojo), table);
  }

  /**
   * Downcasts a jOOQ Record to an UpdatableRecord.
   *
   * @param record the jOOQ record
   * @param table the jOOQ table for the record
   * @return the Record cast to an UpdatableRecord
   * @throws IllegalArgumentException if Record is cannot be downcast to an UpdatableRecord
   */
  private static UpdatableRecord toUpdatable(Record record, Table table) {
    Validate.isInstanceOf(
        UpdatableRecord.class,
        record,
        "Record for table %s is not an instance of UpdatableRecord but is an instance of %s",
        table.getName(),
        record == null ? "null" : record.getClass().getName());
    return (UpdatableRecord) record;
  }

  /**
   * Update a jOOQ table using a POJO.
   *
   * @param dslContext the DSL context
   * @param table the table to update
   * @param pojo the POJO that contains the data
   * @return true if the table was updated
   */
  public static boolean update(DSLContext dslContext, Table table, Object pojo) {
    return update(dslContext, table, pojo, null);
  }

  /**
   * Update a table with a POJO's non-null fields.
   *
   * @param dslContext the DSL context
   * @param table the table
   * @param pojo the POJO containing the data
   * @return true if the table was updated
   */
  public static boolean updateNonNull(DSLContext dslContext, Table table, Object pojo) {
    return update(dslContext, table, pojo, UPDATE_NON_NULL_FIELDS_FILTER);
  }

  /**
   * Update a jOOQ table using a POJO and an optional filter
   *
   * @param dslContext the DSL context
   * @param table the table
   * @param pojo the POJO that contains the data
   * @param filter the filter used to remove fields from the jOOQ record prior to the update, or
   *     null to not use a filter
   * @return true if the corresponding db row was updated
   */
  public static boolean update(
      DSLContext dslContext, Table table, Object pojo, RecordFieldFilter filter) {
    UpdatableRecord<?> record = createUpdatableRecord(dslContext, table, pojo);

    Record primaryKey = record.key();

    Validate.notNull(
        primaryKey.getValue(0),
        "Primary key value in POJO for table %s, Column %s is null",
        table.getName(),
        primaryKey.field(0).getName());

    return (filter == null ? record.update() : record.update(filter.setRecord(record).getFields()))
        > 0;
  }

  /**
   * Validate that the table has one and only 1 PK column and that it is a type of Integer or Long.
   *
   * @param table the table to validate
   * @throws IllegalArgumentException if the table does not meet the constraints
   */
  private static void validatePrimaryKey(Table table) {
    List<Field> primaryKey = getPrimaryKeyFields(table.getPrimaryKey());

    Validate.isTrue(
        primaryKey.size() == 1,
        "Expected %d primary key column, but table %s contains %d",
        1,
        table.getName(),
        primaryKey.size());

    Field<?> field = primaryKey.get(0);

    Validate.isTrue(
        Long.class == field.getType() || Integer.class == field.getType(),
        "Expected an Integer or Long as the primary key Java class type for table %s, but found %s",
        table.getName());
  }

  /**
   * Get the primary key fields
   *
   * @param uniqueKey the jOOQ UniqueKey that contains the primary key fields
   * @return a list of primary key fields
   * @throws IllegalArgumentException if the {@code uniqueKey} contains field objects that are not
   *     an instanceOf Field.class
   */
  @SuppressWarnings("unchecked")
  private static List<Field> getPrimaryKeyFields(UniqueKey uniqueKey) {
    List fields = uniqueKey.getFields();
    if (!fields.isEmpty()) {
      Validate.isInstanceOf(
          Field.class,
          fields.get(0),
          "Expected getFields() to return a list containing instanceOf Field.class, but"
              + " found "
              + fields.get(0).getClass().getName());
    }

    return (List<Field>) fields;
  }

  /** Interface for filtering jOOQ fields. */
  public interface FieldFilter {

    Field<?>[] getFields();
  }

  /** Interface used to filter fields from a jOOQ record. */
  public interface RecordFieldFilter extends FieldFilter {

    RecordFieldFilter setRecord(Record record);
  }

  /** Filter used to only include certain fields. */
  public static class InclusiveFields implements FieldFilter {

    private Field<?>[] fields;

    InclusiveFields(Field<?>[] fields) {
      Validate.notNull(fields, "fields cannot be null");
      this.fields = fields;
    }

    @Override
    public Field<?>[] getFields() {
      return fields;
    }
  }

  /** Filter used to exclude certain fields. */
  public static class ExclusiveFields implements FieldFilter {

    private Field<?>[] fields;

    ExclusiveFields(Field<?>... fields) {
      Validate.notNull(fields, "fields cannot be null");
      this.fields = fields;
    }

    @Override
    public Field<?>[] getFields() {
      return fields;
    }
  }

  /** Removes null values from a jOOQ record. */
  public static class RecordNonNullFieldFilter implements RecordFieldFilter {

    private Record record;

    @Override
    public RecordNonNullFieldFilter setRecord(Record record) {
      this.record = record;
      return this;
    }

    @Override
    public Field<?>[] getFields() {
      return Filters.filterNonNull(record);
    }
  }

  /** Removes jOOQ fields from a jOOQ record. */
  public static class Filters {

    private Filters() {}

    /**
     * Filter out all fields that have null values.
     *
     * <p>The following example will create a UserRecord from the contents of the user instance and
     * then issue a database update including only the non-null fields contained in the user object
     * instance and commit the transaction.
     *
     * <p>If all fields except for the primary key were null, the update will still succeed
     * (returning a count of 1), however, none of the database columns will have changed:
     *
     * <pre>
     * User user = ....
     * UserRecord userRecord = jcx.getDslContext().newRecord(USER, user);
     * int count = userRecord.update(filterNonNull(userRecord));
     * jcx.commit();
     * </pre>
     *
     * @param record the record populated with field values retrieved from the database
     * @return array for fields that have non-null values
     */
    public static Field<?>[] filterNonNull(Record record) {
      List<Field<?>> fields = new ArrayList<>();
      for (Field<?> f : record.fields()) {
        final Object v = record.getValue(f);
        if (v != null) {
          fields.add(f);
        }
      }
      return fields.toArray(new Field[fields.size()]);
    }

    /**
     * Filter out fields by field name.
     *
     * <p>The following example will select all users from the database into the user list. The
     * USER.PASSWORD column will not be part of the select statement and the user.password fields
     * will remain null.
     *
     * <pre>
     * List&lt;User&gt; users =
     *    dslContext.select(filterExcludeByName(USER, USER.PASSWORD)).from(USER).fetch().into(User.class);
     * </pre>
     *
     * @param table the source table
     * @param fieldsToExclude fields to excluded from those contained in the table.
     * @return field entries from table that do not include the names of fieldsToExclude
     */
    public static Field<?>[] filterExcludeByName(Table table, Field... fieldsToExclude) {
      Set<String> fieldNamesToExclude = new HashSet<>();

      for (Field f : fieldsToExclude) {
        fieldNamesToExclude.add(f.getName());
      }

      List<Field<?>> result = new ArrayList<>();

      for (Field f : table.fields()) {
        if (!fieldNamesToExclude.contains(f.getName())) {
          result.add(f);
        }
      }

      return result.toArray(new Field[result.size()]);
    }
  }
}
