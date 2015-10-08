package barqsoft.footballscores.widgets;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by sklaiber on 07.10.15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

    private static final String[] SCORE_COL = {
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.LEAGUE_COL,
    };

    public static final int COL_HOME = 1;
    public static final int COL_AWAY = 2;
    public static final int COL_HOME_GOAL = 3;
    public static final int COL_AWAY_GOAL = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(
                        DatabaseContract.scores_table.buildScoreWithDate()
                        , SCORE_COL
                        , DatabaseContract.PATH_DATE
                        , new String[]{Utilies.formatDate()}
                        , null
                );

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                    data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);

                String homeTeam = data.getString(COL_HOME);
                String awayTeam = data.getString(COL_AWAY);
                int goalHomeTeam = data.getInt(COL_HOME_GOAL);
                int goalAwayTeam = data.getInt(COL_AWAY_GOAL);

                views.setTextViewText(R.id.home_name, homeTeam);
                views.setTextViewText(R.id.away_name, awayTeam);

                Log.d(LOG_TAG, String.valueOf(goalHomeTeam));

                views.setTextViewText(R.id.home_score, String.valueOf(goalHomeTeam));
                views.setTextViewText(R.id.away_score, String.valueOf(goalAwayTeam));

                Intent fillInIntent = new Intent();
                Uri uri = DatabaseContract.scores_table.buildScoreWithDate();
                fillInIntent.setData(uri);

                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(1);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
