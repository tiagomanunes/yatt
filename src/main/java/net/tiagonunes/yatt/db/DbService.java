package net.tiagonunes.yatt.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import net.tiagonunes.yatt.db.persisters.LocalDatePersister;
import net.tiagonunes.yatt.db.persisters.LocalTimePersister;
import net.tiagonunes.yatt.model.Category;
import net.tiagonunes.yatt.model.WorkDone;
import net.tiagonunes.yatt.model.WorkPlanned;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class DbService {

    private static final String DB_DIR = "db";
    private static final String DB_BACKUP_DIR = DB_DIR + File.separator + "backup";

    private static final String PROD_DB = DB_DIR + File.separator + "prod.db";
    private static final String TEST_DB = DB_DIR + File.separator + "test.db";

    private static final String PROD_DB_URL = "jdbc:sqlite:" + PROD_DB;
    private static final String TEST_DB_URL = "jdbc:sqlite:" + TEST_DB;

    private static class StaticHolder {
        static final DbService INSTANCE = new DbService();
    }

    public static DbService get() {
        return StaticHolder.INSTANCE;
    }

    private boolean isTest = true;

    private ConnectionSource connectionSource;
    private Dao<Category, Long> categoryDao;
    private Dao<WorkPlanned, Long> workPlannedDao;
    private Dao<WorkDone, Long> workDoneDao;


    public void init() throws SQLException {
        ensureDirs();
        backup();

        String url = isTest ? TEST_DB_URL : PROD_DB_URL;
        connectionSource = new JdbcConnectionSource(url);

        setup();
    }

    private void ensureDirs() {
        File dbDir = new File(DB_BACKUP_DIR);
        if (!dbDir.mkdirs()) {
            System.out.println("Could not create db directories! Things will start failing soon...");
        }
    }

    public void shutdown() throws Exception {
        connectionSource.close();
    }

    public List<WorkPlanned> reloadWorkPlannedForDay() {
        try {
            QueryBuilder<WorkPlanned, Long> queryBuilder = workPlannedDao.queryBuilder();
            queryBuilder.orderBy("startTime", true).where().eq("date", LocalDate.now());

            PreparedQuery<WorkPlanned> preparedQuery = queryBuilder.prepare();
            return workPlannedDao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void insertWorkPlanned(WorkPlanned work) throws SQLException {
        workPlannedDao.create(work);
    }

    public List<WorkDone> reloadWorkDoneForDay() {
        try {
            QueryBuilder<WorkDone, Long> queryBuilder = workDoneDao.queryBuilder();
            queryBuilder.orderBy("startTime", true).where().eq("date", LocalDate.now());

            PreparedQuery<WorkDone> preparedQuery = queryBuilder.prepare();
            return workDoneDao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void insertWorkDone(WorkDone work) throws SQLException {
        workDoneDao.create(work);
    }

    public List<Category> reloadCategories() {
        try {
            return categoryDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList(); // FIXME create default category
        }
    }

    public void insertCategory(Category category) throws Exception {
        categoryDao.create(category);
    }

    public LocalTime getEarliestAvailableTime() {
        try {
            QueryBuilder<WorkPlanned, Long> queryBuilder = workPlannedDao.queryBuilder();
            queryBuilder.orderBy("startTime", false).where().eq("date", LocalDate.now());

            PreparedQuery<WorkPlanned> preparedQuery = queryBuilder.prepare();
            WorkPlanned workPlanned = workPlannedDao.queryForFirst(preparedQuery);
            return workPlanned.getStartTime().plusMinutes(workPlanned.getDuration());
        } catch (SQLException e) {
            e.printStackTrace();
            return LocalTime.of(8,0);
        }
    }

    public void setIsTest(boolean isTest) {
        this.isTest = isTest;
    }

    private void setup() throws SQLException {
        DataPersisterManager.registerDataPersisters(LocalDatePersister.getSingleton());
        DataPersisterManager.registerDataPersisters(LocalTimePersister.getSingleton());

        TableUtils.createTableIfNotExists(connectionSource, Category.class);
        TableUtils.createTableIfNotExists(connectionSource, WorkPlanned.class);
        TableUtils.createTableIfNotExists(connectionSource, WorkDone.class);

        categoryDao = DaoManager.createDao(connectionSource, Category.class);
        workPlannedDao = DaoManager.createDao(connectionSource, WorkPlanned.class);
        workDoneDao = DaoManager.createDao(connectionSource, WorkDone.class);
    }

    private void backup() {
        File toBackup = new File(PROD_DB);
        if (toBackup.exists()) {
            try {
                String suffix = isTest ? "-test.db" : "-prod.db";
                Files.copy(
                        toBackup.toPath(),
                        Paths.get(DB_BACKUP_DIR + File.separator + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + suffix));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
