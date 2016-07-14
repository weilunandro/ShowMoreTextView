package org.android.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;


public class ShowMoreTextView extends TextView {

    private static final String TAG = "ShowMoreTextView";

    private static final String DEFAULT_ELLIPSE = "...";


    /* 保存微博原文 */
    private CharSequence mOriginText = null;


    /* 要求显示的最大行数 */
    private int mShowMoreMaxLines = -1;

    /* 自定义的文字结尾符号,默认为... */
    @NonNull
    private String mEllipse = DEFAULT_ELLIPSE;

    /* 显示全文的文案 */
    @NonNull
    private String mShowMore = "全文";

    private int mShowMoreTextSize;

    private int mShowMoreTextColor;

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

        mShowMoreMaxLines = 4;
        mShowMoreTextSize = 12;
        mShowMoreTextColor = Color.BLUE;

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
                Log.d(TAG, "onPreDraw: The head " + mShowMoreMaxLines + "  adjust lines contents is " + adjustContent);

                setText(adjustContent);
                getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });
    }


    /**
     * 调整显示内容。截取原来文本,是其加上省略符,省略符和显示全文之间的空格以及显示全文,能够在layout的指定行内显示。
     *
     * @param layout 显示文本的布局
     * @param content 原本要显示的内容
     * @param showMoreMaxLines 要求显示的行数
     * @param mEllipse 省略符
     * @param mShowMore 显示全文文案
     * @param space 省略符和显示全文文案之间的空格数
     * @return 在layout中,可以在指定行内显示的文案
     */
    private SpannableString adjustContent(Layout layout, String content, int showMoreMaxLines, String mEllipse, String mShowMore, int space) {

        /* 拼接尾部信息 */
        StringBuilder builder = new StringBuilder(mEllipse);
        for (int i = 0; i < space; i++) {
            builder.append(" ");
        }
        builder.append(mShowMore);
        String end = builder.toString();


        /* 初始化需要用到的span 因为要重复设置所以先初始化出来,避免实例化多遍 */
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(mShowMoreTextColor);
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(mShowMoreTextSize, true);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                setText(mOriginText);
            }
        };

        /* 为字符串设置span */
        SpannableString adjustContent = new SpannableString(content + end);
        adjustContent.setSpan(colorSpan, adjustContent.length() - mShowMore.length(), adjustContent.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        adjustContent.setSpan(sizeSpan, adjustContent.length() - mShowMore.length(), adjustContent.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        adjustContent.setSpan(clickableSpan, adjustContent.length() - mShowMore.length(), adjustContent.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);


        /* 测量大小,并不断截取原始字符串,行数为showMoreMaxLines为止 */
        DynamicLayout tempLayout = new DynamicLayout(adjustContent, layout.getPaint(), layout.getWidth(), layout.getAlignment(), layout.getSpacingMultiplier(), layout.getSpacingAdd(), false);
        int iterCount = 0;
        int lineCount = tempLayout.getLineCount();
        while (lineCount > showMoreMaxLines) {
            iterCount++;

            adjustContent = new SpannableString(content.substring(0, content.length() - iterCount) + end);
            adjustContent.setSpan(colorSpan, adjustContent.length() - mShowMore.length(), adjustContent.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            adjustContent.setSpan(sizeSpan, adjustContent.length() - mShowMore.length(), adjustContent.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            adjustContent.setSpan(clickableSpan, adjustContent.length() - mShowMore.length(), adjustContent.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

            tempLayout = new DynamicLayout(adjustContent, layout.getPaint(), layout.getWidth(), layout.getAlignment(), layout.getSpacingMultiplier(), layout.getSpacingAdd(), false);
            lineCount = tempLayout.getLineCount();
        }


        return adjustContent;

    }

    /**
     * 获取layout中的前面指定行的内容
     * @param layout 获取内容的layout
     * @param requireMaxLines 指定的行数
     * @return 返回前requireMaxLines的内容
     */
    private String getContent(@NonNull  Layout layout, int requireMaxLines) {

        int maxLines = layout.getLineCount();
        if(requireMaxLines < 0 ){
            return null;
        }
        requireMaxLines = requireMaxLines > maxLines ? maxLines : requireMaxLines;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < requireMaxLines; i++) {
            builder.append(getLineContent(layout, i));
        }
        return builder.toString();
    }


    /**
     * 获取layout中某一行的内容
     * @param layout 获取内容的layout
     * @param i 指定的行数
     * @return layout中行数的内容,如果指定行数超过layout的返回,返回null
     */
    @Nullable
    private String getLineContent(@NonNull Layout layout, int i) {
        if( i <  0 || i >=  layout.getLineCount())
            return null;
        int begin = layout.getLineStart(i);
        int end = layout.getLineEnd(i);
        return layout.getText().subSequence(begin, end).toString();
    }

}
