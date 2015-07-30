package com.itic.mobile.zfyj.qh.contacts.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.itic.mobile.base.ui.widget.ObservableScrollView;
import com.itic.mobile.util.ui.TextViewUtils;
import com.itic.mobile.util.ui.UIUtils;
import com.itic.mobile.zfyj.qh.R;
import com.itic.mobile.zfyj.qh.base.BaseActivityImpl;
import com.itic.mobile.zfyj.qh.provider.Contract;

public class ContactDetailActivity extends BaseActivityImpl
        implements LoaderManager.LoaderCallbacks<Cursor>,ObservableScrollView.Callbacks {

    public static final String TRANSITION_NAME_PHOTO = "photo";

    private static final float PHOTO_ASPECT_RATIO = 1.7777777f;

    private ObservableScrollView mScrollView;
    private View mScrollViewChild;

    private View mPhotoViewContainer;
    private ImageView mPhotoView;
    private int mContactColor;

    private View mHeaderBox;
    private TextView mContactName;
    private String mContactNameString;

    private View mDetailsContainer;
    private View mPhoneContainer;
    private View mCellContainer;
    private TextView mCellPhone;
    private ImageButton mSmsBtn;
    private String mCellPhoneString;
    private String mIMSIString;
    private View mTelOfficeContainr;
    private TextView mTelOffice;
    private String mTelOfficeString;
    private View mTelHomeContainer;
    private TextView mTelHome;
    private String mTelHomeString;
    private View mOrgContainer;
    private TextView mOrg;
    private String mOrgString;
    private View mPostContainer;
    private TextView mPost;
    private String mPostString;
    private View mEmailContainer;
    private TextView mEmail;
    private String mEmailString;
    private View mFaxContainer;
    private TextView mFax;
    private String mFaxString;

    private Handler mHandler = new Handler();

    private Uri mContactUri;
    private String mContactId;

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            recomputePhotoAndScrollingMetrics();
        }
    };
    private int mMaxHeaderElevation;
    private boolean mContactCursor = false;

    private void recomputePhotoAndScrollingMetrics() {
        mHeaderHeightPixels = mHeaderBox.getHeight();

        //计算PhotoViewContainer的Height
        mPhotoHeightPixels = (int) (mPhotoView.getWidth()/PHOTO_ASPECT_RATIO);
        mPhotoHeightPixels = Math.min(mPhotoHeightPixels,mScrollView.getHeight() * 2 / 3);

        ViewGroup.LayoutParams lp;
        lp = mPhotoViewContainer.getLayoutParams();
        if (lp.height != mPhotoHeightPixels){
            lp.height = mPhotoHeightPixels;
            mPhotoViewContainer.setLayoutParams(lp);
        }

        //计算DetailContiner的TopMargin
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mDetailsContainer.getLayoutParams();
        if (mlp.topMargin != mHeaderHeightPixels + mPhotoHeightPixels) {
            mlp.topMargin = mHeaderHeightPixels + mPhotoHeightPixels;
            mDetailsContainer.setLayoutParams(mlp);
        }
        //触发滑动操作
        onScrollChanged(0,0);
    }

    private int mHeaderHeightPixels;
    private int mPhotoHeightPixels = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean shouldBeFloatingWindow = shouldBeFloatingWindow();
        if (shouldBeFloatingWindow) {
            setupFloatingWindow();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        final Toolbar toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("");
            }
        });


        mContactUri = getIntent().getData();

        if (mContactUri == null){
            return;
        }

        mContactId = Contract.Contacts.getContactId(mContactUri);

        mMaxHeaderElevation = getResources().getDimensionPixelSize(
                R.dimen.session_detail_max_header_elevation);
        mHandler = new Handler();

        mScrollView = (ObservableScrollView) findViewById(R.id.scroll_view);
        mScrollView.addCallbacks(this);
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()){
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }

        mScrollViewChild = findViewById(R.id.scroll_view_child);
        mScrollViewChild.setVisibility(View.INVISIBLE);

        mDetailsContainer = findViewById(R.id.details_container);
        mHeaderBox = findViewById(R.id.header_contact);
        mContactName = (TextView) findViewById(R.id.contact_name);
        mContactName.setPadding(0,UIUtils.calculateActionBarSize(this),0,0);
        mPhotoViewContainer = findViewById(R.id.contact_photo_container);
        mPhotoView = (ImageView) findViewById(R.id.contact_photo);
        mPhoneContainer = findViewById(R.id.phone_container);
        mCellContainer = findViewById(R.id.tv_cell_phone);
        mCellPhone = (TextView) findViewById(R.id.tv_cell_phone);
        mSmsBtn = (ImageButton) findViewById(R.id.sms_btn);
        mTelOfficeContainr = findViewById(R.id.tel_office_container);
        mTelOffice = (TextView) findViewById(R.id.tv_tel_office);
        mTelHomeContainer = findViewById(R.id.tel_home_container);
        mTelHome = (TextView) findViewById(R.id.tv_tel_home);
        mOrgContainer = findViewById(R.id.org_container);
        mOrg = (TextView) findViewById(R.id.tv_org);
        mPostContainer = findViewById(R.id.post_container);
        mPost = (TextView) findViewById(R.id.tv_post);
        mEmailContainer = findViewById(R.id.email_container);
        mEmail = (TextView) findViewById(R.id.tv_email);
        mFaxContainer = findViewById(R.id.fax_container);
        mFax = (TextView) findViewById(R.id.tv_fax);

        ViewCompat.setTransitionName(mPhotoView,TRANSITION_NAME_PHOTO);
        LoaderManager manager = getLoaderManager();
        manager.initLoader(ContactQuery._TOKEN,null,this);
    }

    private void setupFloatingWindow() {
        // configure this Activity as a floating window, dimming the background
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getResources().getDimensionPixelSize(R.dimen.session_details_floating_width);
        params.height = getResources().getDimensionPixelSize(R.dimen.session_details_floating_height);
        params.alpha = 1;
        params.dimAmount = 0.4f;
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setAttributes(params);
    }

    private boolean shouldBeFloatingWindow() {
        Resources.Theme theme = getTheme();
        TypedValue floatingWindowFlag = new TypedValue();
        if (theme == null || !theme.resolveAttribute(R.attr.isFloatingWindow, floatingWindowFlag, true)) {
            // isFloatingWindow flag is not defined in theme
            return false;
        }
        return (floatingWindowFlag.data != 0);
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        //更新HeaderBox位置
        //获取ScrollView当前滑动的距离
        int scrollY = mScrollView.getScrollY();
        //取PhotoViewContiner和scrollY之中较大的值为headerbox当前最新的位置
        float  newTop = Math.max(mPhotoHeightPixels,scrollY);
        mHeaderBox.setTranslationY(newTop);

        //计算mHeaderBox 阴影
        float gapFillProgress = 1;
        if (mPhotoHeightPixels != 0){
            gapFillProgress = Math.min(Math.max(UIUtils.getProgress(scrollY,0,mPhotoHeightPixels),0),1);
        }
        ViewCompat.setElevation(mHeaderBox,gapFillProgress * mMaxHeaderElevation);

        //PhotoViewContiner的滑动速率是HeaderBox的0.5倍
        mPhotoViewContainer.setTranslationY(scrollY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScrollView == null){
            return;
        }

        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()){
            vto.removeGlobalOnLayoutListener(mGlobalLayoutListener);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if (id == ContactQuery._TOKEN){
            loader = new CursorLoader(this,mContactUri,ContactQuery.PROJECTION,null,null,null);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == ContactQuery._TOKEN){
            onContactQueryComplete(cursor);
        }
    }

    private void onContactQueryComplete(Cursor cursor) {
        mContactCursor = true;
        if (!cursor.moveToFirst()){
            finish();
            return;
        }
        mContactNameString = cursor.getString(ContactQuery.CONTACT_NAME);
        mContactColor = cursor.getInt(ContactQuery.CONTACT_COLOR);
        mCellPhoneString = cursor.getString(ContactQuery.TEL_CELL);
        mTelHomeString = cursor.getString(ContactQuery.TEL_CELL);
        mTelOfficeString = cursor.getString(ContactQuery.TEL_OFFICE);
        mTelHomeString = cursor.getString(ContactQuery.TEL_HOME);
        mOrgString = cursor.getString(ContactQuery.ORG_NAME);
        mPostString = cursor.getString(ContactQuery.POST);
        mEmailString = cursor.getString(ContactQuery.EMAIL);
        mFaxString = cursor.getString(ContactQuery.FAX);
        mIMSIString = cursor.getString(ContactQuery.SIM_IMSI);

        if (mContactColor == 0){
            mContactColor = getResources().getColor(R.color.contact_color_default);
        }else{
            mContactColor = UIUtils.setColorAlpha(mContactColor,255);
        }

        mHeaderBox.setBackgroundColor(mContactColor);
        mPhotoViewContainer.setBackgroundColor(mContactColor);
        setNormalStatusBarColor(UIUtils.scaleColor(mContactColor,0.8f,false));

        mContactName.setText(TextViewUtils.setText(mContactNameString));

        if (TextUtils.isEmpty(mCellPhoneString)){
            mCellContainer.setVisibility(View.GONE);
        }else{
            mCellPhone.setText(mCellPhoneString);
            mCellContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(mIMSIString)){
                        Intent telIntent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+ mCellPhoneString));
                        telIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(telIntent);
                    }else{
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_imsi),Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mSmsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(mIMSIString)){
                        Intent smsIntent = new Intent(Intent.ACTION_SENDTO,Uri.parse("smsto:" + mCellPhoneString));
                        smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(smsIntent);
                    }else{
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_imsi),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (TextUtils.isEmpty(mTelOfficeString)){
            mTelOfficeContainr.setVisibility(View.GONE);
        }else{
            mTelOffice.setText(mTelOfficeString);
            mTelOfficeContainr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent telIntent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+ mTelOfficeString));
                    telIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(telIntent);
                }
            });
        }
        if (TextUtils.isEmpty(mTelHomeString)){
            mTelHomeContainer.setVisibility(View.GONE);
        }else{
            mTelHome.setText(mTelHomeString);
            mTelHomeContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent telIntent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+ mTelHomeString));
                    telIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(telIntent);
                }
            });
        }
        if (TextUtils.isEmpty(mOrgString)){
            mOrgContainer.setVisibility(View.GONE);
        }else{
            mOrg.setText(mOrgString);
        }
        if (TextUtils.isEmpty(mPostString)){
            mPostContainer.setVisibility(View.GONE);
        }else{
            mPost.setText(mPostString);
        }
        if (TextUtils.isEmpty(mEmailString)){
            mEmailContainer.setVisibility(View.GONE);
        }else{
            mEmail.setText(mEmailString);
        }
        if (TextUtils.isEmpty(mFaxString)){
            mFaxContainer.setVisibility(View.GONE);
        }else{
            mFax.setText(mFaxString);
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onScrollChanged(0,0);
                mScrollViewChild.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private interface ContactQuery{
        int _TOKEN = 0x1;

        String[] PROJECTION = {
                Contract.Contacts.CONTACT_NAME,
                Contract.Contacts.CONTACT_COLOR,
                Contract.Contacts.ORG_NAME,
                Contract.Contacts.POST,
                Contract.Contacts.TEL_CELL,
                Contract.Contacts.TEL_OFFICE,
                Contract.Contacts.TEL_HOME,
                Contract.Contacts.SIM_IMSI,
                Contract.Contacts.EMAIL,
                Contract.Contacts.FAX
        };
        int CONTACT_NAME = 0;
        int CONTACT_COLOR = 1;
        int ORG_NAME = 2;
        int POST = 3;
        int TEL_CELL = 4;
        int TEL_OFFICE = 5;
        int TEL_HOME = 6;
        int SIM_IMSI = 7;
        int EMAIL = 8;
        int FAX = 9;
    }
}