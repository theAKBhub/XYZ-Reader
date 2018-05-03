package com.example.xyzreader.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.example.xyzreader.R;

public class ArticleDetailFragment extends Fragment {

    private static final String TAG = ArticleDetailFragment.class.getSimpleName();
    private static final String KEY_ARTICLE_CONTENT = "article_content";

    private Unbinder mUnbinder;
    private View mRootView;

    @BindView(R.id.textview_article)
    TextView mTextViewArticle;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    /**
     * NewInstance constructor for creating fragment with arguments
     */
    public static ArticleDetailFragment newInstance(String article) {
        ArticleDetailFragment articleDetailFragment = new ArticleDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ARTICLE_CONTENT, article);
        articleDetailFragment.setArguments(bundle);
        return articleDetailFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        String article = "";

        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);

        if (getArguments() != null && getArguments().containsKey(KEY_ARTICLE_CONTENT)) {
            article = getArguments().getString(KEY_ARTICLE_CONTENT);
        }

        prepareArticleText(article);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //mTextViewArticle.setText(getArguments().getString(KEY_ARTICLE_CONTENT));
    }

    public void prepareArticleText(String articleText) {
        String a = articleText.replaceAll(">", "&gt;");
        String a1=a.replaceAll("(\r\n){2}(?!(&gt;))", "<br><br>");
        String a2=a1.replaceAll("(\r\n)"," ");

        //remove all text between [ and ]
        String a3=a2.replaceAll("\\[.*?\\]","");

        //put new line after i.e 1. Ebooks aren't marketing.
        String a4=a3.replaceAll("(\\d\\.\\s.*?\\.)","$1<br>");

        //make text between * * bold
        String a5=a4.replaceAll("\\*(.*?)\\*", "<b>$1</b>");

        //remove all '>' from text such as 'are >'  but leave the first '>' in tact
        String a6=a5.replaceAll("(\\w\\s)&gt;", "$1");

        Spanned a7= Html.fromHtml(a6);

        mTextViewArticle.setText(a7.toString());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

}
