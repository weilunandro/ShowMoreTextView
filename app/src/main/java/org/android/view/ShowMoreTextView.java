package org.android.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

/**
 * Created by weilun on 16/7/8.
 */

public class ShowMoreTextView extends TextView {

    private static final String TAG = ShowMoreTextView.class.getSimpleName();

    private static final String DEFAULT_ELLIPSE = "...";

    private CharSequence mOriginText = null;

    private int mShowMoreMaxLines = -1;

    @NonNull
    private String mEllipse = DEFAULT_ELLIPSE;
    @NonNull
    private String mShowMore = "全文";


    public ShowMoreTextView(Context context) {
        super(context);

        init(context);
    }


    public ShowMoreTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        setEllipsize(null);
        mOriginText = getText();
        setMovementMethod(LinkMovementMethod.getInstance());

        mShowMoreMaxLines = 3;

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {

                // Layout layout = getLayout();
                Layout originLayout = getLayout();

                DynamicLayout layout = new DynamicLayout(getText(), originLayout.getPaint(), originLayout.getWidth(), originLayout.getAlignment(), originLayout.getSpacingMultiplier(), originLayout.getSpacingAdd(), false);
                Log.d(TAG, "The origin text is " + getText());

                String content = getContent(layout, mShowMoreMaxLines);
                Log.d(TAG, "onPreDraw: The head " + mShowMoreMaxLines + " lines contents is " + content);

                SpannableString adjustContent = adjustContent(layout, content, mShowMoreMaxLines, mEllipse, mShowMore, 3);
                Log.d(TAG, "onPreDraw: The head " + mShowMoreMaxLines + "  adjust lines contents is " + adjustContent );

                setText(adjustContent);
                getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });
    }

    private SpannableString adjustContent(Layout layout, String content, int mShowMoreMaxLines, String mEllipse, String mShowMore, int space) {

        StringBuilder builder = new StringBuilder(mEllipse);
        for (int i = 0; i < space; i++){
            builder.append(" ");
        }
        builder.append(mShowMore);
        String end = builder.toString();

        String adjustContent = content + end;
        StaticLayout tempLayout = new StaticLayout( adjustContent, layout.getPaint(), layout.getWidth(), layout.getAlignment(), layout.getSpacingMultiplier(), layout.getSpacingAdd(), false);

        int iterCount = 0;
        int lineCount = tempLayout.getLineCount();
        while ( lineCount > mShowMoreMaxLines){
            iterCount ++ ;

            adjustContent = content.substring(0 , content.length() - iterCount) + end;
            tempLayout = new StaticLayout( adjustContent, layout.getPaint(), layout.getWidth(), layout.getAlignment(), layout.getSpacingMultiplier(), layout.getSpacingAdd(), false);
            lineCount  = tempLayout.getLineCount();
        }

        SpannableString result = new SpannableString(adjustContent);
        result.setSpan(new ForegroundColorSpan(Color.RED), adjustContent.length() - mShowMore.length(), adjustContent.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        result.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                setText(mOriginText);
                setMaxLines(Integer.MAX_VALUE);
                requestLayout();
            }
        }, adjustContent.length() - mShowMore.length(), adjustContent.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return result;

    }

    private String getContent(Layout layout, int requireMaxLines) {
        int maxLines = layout.getLineCount();
        requireMaxLines = requireMaxLines > maxLines ? maxLines : requireMaxLines;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < requireMaxLines; i++) {
            builder.append(getLineContent(layout, i));
        }
        return builder.toString();
    }


    @NonNull
    private String getLineContent(Layout layout, int i) {
        int begin = layout.getLineStart(i);
        int end = layout.getLineEnd(i);
        return layout.getText().subSequence(begin, end).toString();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }
}
