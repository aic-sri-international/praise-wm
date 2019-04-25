package com.sri.ai.praisewm.db.internal;

import static org.jooq.conf.ParamType.INLINED;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;
import org.jooq.impl.DefaultBinding;

/** Provides access to the JDBC connection and low-level transaction processing. */
public class TimestampUtcBinding implements Binding<Timestamp, Instant> {
  // @TODO delete me once JOOQ supports java.time.Instant string conversion - but make
  // sure it works correctly with MySQL DateTime datatype !
  //
  private static final long serialVersionUID = 1;

  private final Converter<Timestamp, Instant> converter;
  private final Binding<Timestamp, Instant> delegate;

  public TimestampUtcBinding() {
    this.converter =
        new Converter<>() {
          @Override
          public Instant from(Timestamp databaseObject) {
            if (databaseObject == null) {
              return null;
            }
            return databaseObject.toInstant();
          }

          @Override
          public Timestamp to(Instant userObject) {
            if (userObject == null) {
              return null;
            }
            return Timestamp.from(userObject);
          }

          @Override
          public Class<Timestamp> fromType() {
            return Timestamp.class;
          }

          @Override
          public Class<Instant> toType() {
            return Instant.class;
          }
        };
    this.delegate = DefaultBinding.binding(converter);
  }

  @Override
  public final Converter<Timestamp, Instant> converter() {
    return converter;
  }

  @Override
  public final void sql(BindingSQLContext<Instant> ctx) throws SQLException {
    if (ctx.render().paramType() == INLINED) {
      if (ctx.value() != null) {
        String isoFormat = ctx.value().toString();
        String mySqlFormat = isoFormat.substring(0, 10) + ' ' + isoFormat.substring(11, 19);
        ctx.render().sql(mySqlFormat);
      }
    } else {
      delegate.sql(ctx);
    }
  }

  @Override
  public final void register(BindingRegisterContext<Instant> ctx) throws SQLException {
    delegate.register(ctx);
  }

  @Override
  public final void set(BindingSetStatementContext<Instant> ctx) throws SQLException {
    Instant value = ctx.value();
    PreparedStatement statement = ctx.statement();
    if (value == null) {
      statement.setNull(ctx.index(), Types.TIMESTAMP);
    } else {
      ZonedDateTime zdt = value.atZone(ZoneOffset.UTC);
      Timestamp timestamp = Timestamp.valueOf(zdt.toLocalDateTime());
      statement.setTimestamp(ctx.index(), timestamp, Calendar.getInstance());
    }
  }

  @Override
  public final void set(BindingSetSQLOutputContext<Instant> ctx) throws SQLException {
    delegate.set(ctx);
  }

  @Override
  public final void get(BindingGetResultSetContext<Instant> ctx) throws SQLException {
    ResultSet resultSet = ctx.resultSet();
    Calendar calendar = Calendar.getInstance();
    Timestamp timestamp = resultSet.getTimestamp(ctx.index(), calendar);

    if (timestamp == null) {
      ctx.value(null);
    } else {
      ZonedDateTime zdt = timestamp.toLocalDateTime().atZone(ZoneOffset.UTC);
      ctx.value(zdt.toInstant());
    }
  }

  @Override
  public final void get(BindingGetStatementContext<Instant> ctx) throws SQLException {
    delegate.get(ctx);
  }

  @Override
  public final void get(BindingGetSQLInputContext<Instant> ctx) throws SQLException {
    delegate.get(ctx);
  }
}
