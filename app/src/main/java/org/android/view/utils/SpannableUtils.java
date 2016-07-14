package org.android.view.utils;

import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;

/**
 * Created by weilun on 16/7/14.
 */

public class SpannableUtils {

    public static SpannableStringBuilder combineStringAndSpannable(@NonNull  String prefix, @NonNull  Spannable spannableString){
        SpannableStringBuilder builder = new SpannableStringBuilder(prefix);
        builder.append(spannableString.toString());

        int start = prefix.length();
        int end = builder.length();
        Object [] spanneds = spannableString.getSpans(0, spannableString.length(), Object.class);
        for(Object spanned : spanneds){
            builder.setSpan(spanned, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        return builder;
    }
}
