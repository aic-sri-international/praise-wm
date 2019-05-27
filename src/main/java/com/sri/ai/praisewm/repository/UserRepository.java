package com.sri.ai.praisewm.repository;

import static com.sri.ai.praisewm.db.jooq.Tables.USER;

import com.sri.ai.praisewm.db.JooqContext;
import com.sri.ai.praisewm.db.JooqDbUtil;
import com.sri.ai.praisewm.db.jooq.tables.daos.UserDao;
import com.sri.ai.praisewm.db.jooq.tables.pojos.User;
import com.sri.ai.praisewm.service.SecurityServiceImpl;
import java.util.List;
import org.jooq.Record;
import org.jooq.Result;

/** UserRepository. */
public class UserRepository {
  public static List<User> getUsers(JooqContext jooqContext) {
    return getUsersRecordlist(jooqContext).into(User.class);
  }

  public static String getUsersAsCsv(JooqContext jooqContext) {
    return getUsersRecordlist(jooqContext).formatCSV();
  }

  private static Result<Record> getUsersRecordlist(JooqContext jooqContext) {
    return jooqContext
        .dslContext()
        .select(JooqDbUtil.Filters.filterExcludeByName(USER, USER.PASSWORD))
        .from(USER)
        .orderBy(USER.NAME)
        .fetch();
  }

  public static User getUserAndPasswordByName(JooqContext jooqContext, String username) {
    return new UserDao(jooqContext.configuration()).fetchOneByName(username);
  }

  public static User getUser(JooqContext jooqContext, int id) {
    User user = new UserDao(jooqContext.configuration()).fetchOneByUserId(id);
    if (user != null) {
      user.setPassword(null);
    }
    return user;
  }

  public static Number createUser(JooqContext jooqContext, User user) {
    return JooqDbUtil.insert(jooqContext.dslContext(), USER, user);
  }

  public static boolean updateUser(JooqContext jooqContext, User user) {
    return JooqDbUtil.updateNonNull(jooqContext.dslContext(), USER, user);
  }

  public static boolean deleteUser(JooqContext jooqContext, String id) {
    // Do not allow the special admin user to be deleted.
    int count =
        jooqContext
            .dslContext()
            .deleteFrom(USER)
            .where(
                USER.USER_ID
                    .equal(USER.USER_ID.getDataType().convert(id))
                    .and(USER.NAME.notEqual(SecurityServiceImpl.ADMIN_NAME)))
            .execute();
    return (count > 0);
  }
}
