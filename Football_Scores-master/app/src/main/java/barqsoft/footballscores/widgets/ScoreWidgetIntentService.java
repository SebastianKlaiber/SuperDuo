package barqsoft.footballscores.widgets;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by sklaiber on 06.10.15.
 */
public class ScoreWidgetIntentService extends IntentService {

    private static final String[] SCORE_COL = {
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
    };

    public static final int COL_HOME = 1;
    public static final int COL_AWAY = 2;
    public static final int COL_HOME_GOAL = 3;
    public static final int COL_AWAY_GOAL = 4;


    public ScoreWidgetIntentService() {
        super("ScoreWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                ScoreWidget.class));

        Cursor cursor = getContentResolver().query(
                DatabaseContract.scores_table.buildScoreWithDate()
                ,SCORE_COL
                ,DatabaseContract.PATH_DATE
                ,new String[]{Utilies.formatDate()}
                ,null
        );

        if (cursor == null) {
            return;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.score_app_widget);
            String scores = Utilies.getScores(cursor.getInt(COL_HOME_GOAL), cursor.getInt(COL_AWAY_GOAL));

            views.setTextViewText(R.id.home_name, cursor.getString(COL_HOME));
            views.setTextViewText(R.id.away_name, cursor.getString(COL_AWAY));
            views.setTextViewText(R.id.score_textview, scores);

            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        cursor.close();
    }
    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }
}
