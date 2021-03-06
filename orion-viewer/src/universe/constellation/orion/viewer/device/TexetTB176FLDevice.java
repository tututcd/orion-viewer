package universe.constellation.orion.viewer.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import universe.constellation.orion.viewer.LastPageInfo;
import universe.constellation.orion.viewer.OrionBaseActivity;

/**
 * User: mike
 * Date: 1/12/14
 * Time: 1:07 PM
 */
public class TexetTB176FLDevice extends AndroidDevice {

    private OrionBaseActivity activity;

    @Override
    public void onCreate(OrionBaseActivity activity) {
        super.onCreate(activity);
        this.activity = activity;
    }

    @Override
    public boolean isDefaultDarkTheme() {
        return false;
    }

    @Override
    public void onNewBook(LastPageInfo info) {
        try {
            shtampTexetFile(info.openingFileName, info.simpleFileName, "", "" + info.totalPages, "" + info.pageNumber, "");
        } catch (Exception e) {
            Toast.makeText(activity, "Error on new book parameters update: " + e.getMessage(), Toast.LENGTH_SHORT).show();;
        }
    }

    @Override
    public void onBookClose(LastPageInfo info) {
        super.onBookClose(info);

        try {
            shtampTexetFile(null, null, null, "" + info.totalPages, "" + info.pageNumber, "");
        } catch (Exception e) {
            Toast.makeText(activity, "Error on parameters update on book close: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //code provided by texet
    public void shtampTexetFile(String cPath, String cTitle, String cAuthor, String cAllPage, String cCurPage, String cCover) throws Exception {
        if (cCover != null)
            Log.e("COVER", cCover);

        Time myTime = new Time(Time.getCurrentTimezone());
        myTime.setToNow();
        String cDate = String.format("%02d/%02d/%02d %02d:%02d",
                myTime.monthDay, myTime.month + 1, myTime.year, myTime.hour, myTime.minute, myTime.second);

        Context bmkContext =
                activity.getApplicationContext().createPackageContext("com.android.systemui", Context.CONTEXT_IGNORE_SECURITY);


        SharedPreferences settings =
                bmkContext.getSharedPreferences("MyPrefsFile",
                        Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor = null;

        if (cPath == null) {
            editor = settings.edit();

            editor.putString("FirstRecentTotalPage", cAllPage);
            editor.putString("FirstRecentPage", cCurPage);
            editor.putString("FirstRecentReadDate", cDate);

            editor.commit();
            return;
        }

        String curRecentReadingPath =
                settings.getString("FirstRecentPath", "");
        if (cPath.contentEquals(curRecentReadingPath)) {
            return;
        }

        editor = settings.edit();

        String firstRecentPath = settings.getString("FirstRecentPath",
                "");
        String firstRecentTitle =
                settings.getString("FirstRecentTitle", "");
        String firstRecentAuthor =
                settings.getString("FirstRecentAuthor", "");
        String firstRecentTotalPage =
                settings.getString("FirstRecentTotalPage", "");
        String firstRecentPage = settings.getString("FirstRecentPage", "");
        String firstRecentDate =
                settings.getString("FirstRecentReadDate", "");
        String firstBookCover = settings.getString("FirstBookCover", "");

        String secondRecentPath =
                settings.getString("SecondRecentPath", "");
        String secondRecentTitle =
                settings.getString("SecondRecentTitle", "");
        String secondRecentAuthor =
                settings.getString("SecondRecentAuthor", "");
        String secondRecentTotalPage =
                settings.getString("SecondRecentTotalPage", "");
        String secondRecentPage =
                settings.getString("SecondRecentPage", "");
        String secondRecentDate =
                settings.getString("SecondRecentReadDate", "");
        String secondBookCover = settings.getString("SecondBookCover", "");

        String thirdRecentPath = settings.getString("ThirdRecentPath",
                "");
        String thirdRecentTitle =
                settings.getString("ThirdRecentTitle", "");
        String thirdRecentAuthor =
                settings.getString("ThirdRecentAuthor", "");
        String thirdRecentTotalPage =
                settings.getString("ThirdRecentTotalPage", "");
        String thirdRecentPage = settings.getString("ThirdRecentPage",
                "");
        String thirdRecentDate =
                settings.getString("ThirdRecentReadDate", "");
        String thirdBookCover = settings.getString("ThirdBookCover", "");

        if (cPath.contentEquals(secondRecentPath)) {
            secondRecentPath = firstRecentPath;
            secondRecentTitle = firstRecentTitle;
            secondRecentAuthor = firstRecentAuthor;
            secondRecentTotalPage = firstRecentTotalPage;
            secondRecentPage = firstRecentPage;
            secondRecentDate = firstRecentDate;
            secondBookCover = firstBookCover;
        } else {
            thirdRecentPath = secondRecentPath;
            thirdRecentTitle = secondRecentTitle;
            thirdRecentAuthor = secondRecentAuthor;
            thirdRecentTotalPage = secondRecentTotalPage;
            thirdRecentPage = secondRecentPage;
            thirdRecentDate = secondRecentDate;
            thirdBookCover = secondBookCover;

            secondRecentPath = firstRecentPath;
            secondRecentTitle = firstRecentTitle;
            secondRecentAuthor = firstRecentAuthor;
            secondRecentTotalPage = firstRecentTotalPage;
            secondRecentPage = firstRecentPage;
            secondRecentDate = firstRecentDate;
            secondBookCover = firstBookCover;
        }

        firstRecentPath = cPath;
        firstRecentTitle = cTitle;
        firstRecentAuthor = cAuthor;
        firstRecentTotalPage = cAllPage;
        firstRecentPage = cCurPage;
        firstRecentDate = cDate;
        firstBookCover = cCover;

        editor.putString("FirstRecentPath", firstRecentPath);
        editor.putString("FirstRecentTitle", firstRecentTitle);
        editor.putString("FirstRecentAuthor", firstRecentAuthor);
        editor.putString("FirstRecentTotalPage", firstRecentTotalPage);
        editor.putString("FirstRecentPage", firstRecentPage);
        editor.putString("FirstRecentReadDate", firstRecentDate);
        editor.putString("FirstBookCover", firstBookCover);

        editor.putString("SecondRecentPath", secondRecentPath);
        editor.putString("SecondRecentTitle", secondRecentTitle);
        editor.putString("SecondRecentAuthor", secondRecentAuthor);
        editor.putString("SecondRecentTotalPage", secondRecentTotalPage);
        editor.putString("SecondRecentPage", secondRecentPage);
        editor.putString("SecondRecentReadDate", secondRecentDate);
        editor.putString("SecondBookCover", secondBookCover);

        editor.putString("ThirdRecentPath", thirdRecentPath);
        editor.putString("ThirdRecentTitle", thirdRecentTitle);
        editor.putString("ThirdRecentAuthor", thirdRecentAuthor);
        editor.putString("ThirdRecentTotalPage", thirdRecentTotalPage);
        editor.putString("ThirdRecentPage", thirdRecentPage);
        editor.putString("ThirdRecentReadDate", thirdRecentDate);
        editor.putString("ThirdBookCover", thirdBookCover);

        editor.commit();

    }
}
