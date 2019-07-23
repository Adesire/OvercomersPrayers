package com.overcomersprayer.app.overcomersprayers.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.overcomersprayer.app.overcomersprayers.R;

import com.overcomersprayer.app.overcomersprayers.models.ListOfCategoriesWithHeading;
import com.overcomersprayer.app.overcomersprayers.models.ListOfCategoriesWithHeading.CategoryWithHeadings;
import com.overcomersprayer.app.overcomersprayers.models.ListOfCategoriesWithHeading.PrayerHeadings;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OpHelper {
    public static void readOpDoc(Context context) {
        InputStream fileStream = context.getResources().openRawResource(R.raw.op_contents2);
        String ovpr = readTextFile(fileStream);

        String latestCategory = null;
        List<CategoryWithHeadings> categoryWithHeadingsList = new ArrayList<>();

        JSONObject prayerBook = null;
        try {
            prayerBook = new JSONObject(ovpr);

            JSONObject article = prayerBook.getJSONObject("article");
            JSONArray orderedList = article.getJSONArray("orderedlist");
            for (int i = 0; i < orderedList.length(); i++) {
                JSONObject object = orderedList.optJSONObject(i);
                if (i == 0 || i % 2 == 0)
                    latestCategory = parseHeading(object);
                else {
                    List<PrayerHeadings> headings = parseBody(object);
                    CategoryWithHeadings categoryWithHeadings = new CategoryWithHeadings(latestCategory, headings);
                    categoryWithHeadingsList.add(categoryWithHeadings);
                }
            }
            ListOfCategoriesWithHeading listOfCategoriesWithHeading = new ListOfCategoriesWithHeading(categoryWithHeadingsList);
            EventBus.getDefault().post(listOfCategoriesWithHeading);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }

    private static String parseHeading(JSONObject jsonObject) {
        return jsonObject.optJSONObject("listitem").optString("para");
    }

    private static List<PrayerHeadings> parseBody(JSONObject jsonObject) {
        List<PrayerHeadings> headings = new ArrayList<>();
        JSONArray jsonArray = jsonObject.optJSONArray("listitem");
        for (int i = 0; i < jsonArray.length(); i++) {
            headings.add(new PrayerHeadings(jsonArray.optJSONObject(i).optString("para")));
        }
        return headings;
    }
}
