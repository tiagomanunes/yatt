package net.tiagonunes.yatt.db.persisters;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDatePersister extends BaseDataType {

    private static final LocalDatePersister instance = new LocalDatePersister();

    private LocalDatePersister() {
        super(SqlType.STRING, new Class<?>[] { LocalDate.class });
    }

    public static LocalDatePersister getSingleton() {
        return instance;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        if (javaObject == null) {
            return null;
        } else {
            return ((LocalDate) javaObject).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getString(columnPos);
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) {
        return defaultStr;
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        String date = (String)sqlArg;
        if (date == null) {
            return null;
        } else {
            return LocalDate.parse(date);
        }
    }
}