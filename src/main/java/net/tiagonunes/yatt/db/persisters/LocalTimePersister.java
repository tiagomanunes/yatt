package net.tiagonunes.yatt.db.persisters;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimePersister extends BaseDataType {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static final LocalTimePersister instance = new LocalTimePersister();

    private LocalTimePersister() {
        super(SqlType.STRING, new Class<?>[] { LocalTime.class });
    }

    public static LocalTimePersister getSingleton() {
        return instance;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        if (javaObject == null) {
            return null;
        } else {
            return ((LocalTime) javaObject).format(TIME_FORMATTER);
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
        String time = (String)sqlArg;
        if (time == null) {
            return null;
        } else {
            return LocalTime.parse(time, TIME_FORMATTER);
        }
    }
}