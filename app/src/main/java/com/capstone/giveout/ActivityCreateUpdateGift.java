package com.capstone.giveout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.capstone.giveout.base.BaseActivity;
import com.capstone.giveout.base.Routes;
import com.capstone.giveout.base.State;
import com.capstone.giveout.dialogs.BaseRetainedDialog;
import com.capstone.giveout.dialogs.DialogConfirm;
import com.capstone.giveout.dialogs.DialogLoading;
import com.capstone.giveout.dialogs.DialogLogin;
import com.capstone.giveout.dialogs.DialogSelectGiftChain;
import com.capstone.giveout.models.Gift;
import com.capstone.giveout.models.GiftChain;
import com.capstone.giveout.net.Net;
import com.capstone.giveout.net.requests.AuthMultiPartRequest;
import com.capstone.giveout.utils.ImageSampler;
import com.capstone.giveout.utils.SyncManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public class ActivityCreateUpdateGift extends BaseActivity implements DialogLogin.OnLoginListener,
                                                                      DialogSelectGiftChain.OnDialogGiftChainSelectedListener,
                                                                      DialogConfirm.OnDialogConfirmListener {
    private static final String ARG_GIFT_FOR_UPDATE = "ARG_GIFT_FOR_UPDATE";

    private static final String TAG_LOGIN = "ActivityCreateGift - TAG_LOGIN";
    private static final String TAG_CONFIRM_NEW_GIFT_CHAIN = "ActivityCreateGift - TAG_CONFIRM_NEW_GIFT_CHAIN";
    private static final String TAG_SELECT_GIFT_CHAIN = "ActivityCreateGift - TAG_SELECT_GIFT_CHAIN";

    private static final String STATE_IMAGE_URI = "STATE_IMAGE_URI";
    private static final int IMAGE_CAPTURE_REQUEST = 0;
    private static final int IMAGE_PICK_REQUEST = 1;

    private List<GiftChain> giftChains;
    private Uri tempImageUri;
    private Gift giftForUpdate;

    private EditText mGiftChain;
    private Button mSelectGiftChainButton;
    private TextView mGiftTitle;
    private TextView mGiftDescription;
    private ImageView mGiftImage;
    private CheckBox mNoGiftChainCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_gift);
        if (! State.get().isUserLoggedIn()) {
            DialogLogin.open(getFragmentManager(), TAG_LOGIN);
        }

        mGiftTitle = (TextView) findViewById(R.id.gift_title);
        mGiftImage = (ImageView) findViewById(R.id.gift_image);
        mGiftDescription = (TextView) findViewById(R.id.gift_description);
        mGiftChain = (EditText) findViewById(R.id.gift_chain);
        mSelectGiftChainButton = (Button) findViewById(R.id.select_gift_chain_button);
        mNoGiftChainCheck = (CheckBox) findViewById(R.id.no_gift_chain_check);

        mGiftChain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if ( mGiftChain.getTag() != null ) {
                    DialogConfirm.open(getFragmentManager(), TAG_CONFIRM_NEW_GIFT_CHAIN, "If you edit the gift chain name, a new one will be created on save...");
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
        mSelectGiftChainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogSelectGiftChain.open(getFragmentManager(), TAG_SELECT_GIFT_CHAIN);
            }
        });

        mNoGiftChainCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mGiftChain.setEnabled(!isChecked);
                mSelectGiftChainButton.setEnabled(!isChecked);
            }
        });

        ImageButton takePhotoButton = (ImageButton) findViewById(R.id.image_from_camera_button);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCameraIntent();
            }
        });

        ImageButton pickPhotoButton = (ImageButton) findViewById(R.id.image_from_gallery_button);
        pickPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPickImageIntent();
            }
        });

        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataAndSendGift();
            }
        });

        giftForUpdate = (Gift) getIntent().getSerializableExtra(ARG_GIFT_FOR_UPDATE);

        fillFromGfit(giftForUpdate);
    }

    private void fillFromGfit(Gift gift) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mGiftImage.getTag() != null) {
            outState.putString(STATE_IMAGE_URI, String.valueOf(mGiftImage.getTag()));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String savedUriString = savedInstanceState.getString(STATE_IMAGE_URI);
        if (savedUriString != null) {
            Uri imageUri = Uri.parse(savedUriString);
            mGiftImage.setImageURI(imageUri);
            mGiftImage.setTag(imageUri);
        }
    }

    @Override
    protected void onStop() {
        Net.getQueue().cancelAll(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_create_gift, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoginFinish(BaseRetainedDialog dialogFragment, String tag, boolean success) {
        if (!success) {
            finish();
        }
    }

    private void getDataAndSendGift() {
        boolean error = false;
        String giftTitle = String.valueOf(mGiftTitle.getText());
        String giftDescription = String.valueOf(mGiftDescription.getText());
        String giftChainName = String.valueOf(mGiftChain.getText());
        GiftChain giftChain = (GiftChain) mGiftChain.getTag();
        boolean thereMustBeAGiftChain = !mNoGiftChainCheck.isChecked();

        // Check for errors

        if (TextUtils.isEmpty(giftTitle)) {
            mGiftTitle.setError("Please insert a Gift title");
            error = true;
        }

        if (TextUtils.isEmpty(giftDescription)) {
            mGiftDescription.setError("Please insert a Gift description");
            error = true;
        }

        if (thereMustBeAGiftChain && TextUtils.isEmpty(giftChainName)) {
            mGiftChain.setError("Please insert or select a Gift chain");
            error = true;
        }

        Uri giftImageUri = (Uri) mGiftImage.getTag();
        if (giftImageUri == null) {
            Toast.makeText(this, "Please take or select a photo for this Gift",Toast.LENGTH_LONG).show();
            error = true;
        }

        if (error) {
            return;
        }

        // Create the gift chain
        GiftChain newGiftChain = null;
        if (thereMustBeAGiftChain) {
            newGiftChain = new GiftChain();
            if (giftChain != null) {
                newGiftChain.id = giftChain.id;
            } else {
                newGiftChain.name = giftChainName;
            }
        }

        // Create the gift
        Gift gift = new Gift();
        gift.giftChain = newGiftChain;
        gift.title = giftTitle;
        gift.description = giftDescription;

        // Send the gift
        File image = new File(giftImageUri.getPath());
        sendGift(gift, image);
    }

    void sendGift(Gift gift, File image) {
        try {
            String jsonGift = new ObjectMapper().writeValueAsString(gift);

            DialogLoading progressDialog = DialogLoading.open(getFragmentManager());

            String url = Routes.urlFor(Routes.GIFTS_PATH);
            AuthMultiPartRequest<Gift> req = new AuthMultiPartRequest<>(Request.Method.POST, url, Gift.class,
                    new Response.Listener<Gift>() {
                        @Override
                        public void onResponse(Gift response) {
                            DialogLoading.close(getFragmentManager());
                            SyncManager.sendBroadcast(ActivityCreateUpdateGift.this, SyncManager.RELOAD_DATA_ACTION);
                            finish();
                        }
                    },
                    getErrorListener(true, progressDialog)
            );

            req.addStringUpload("gift", jsonGift);
            req.addFileUpload("image", image);
            req.setTag(this);
            Net.addToQueue(req);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void createTempImageUri() {
        try {
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = File.createTempFile("giftImage", ".jpg", storageDir);
            tempImageUri = Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void launchCameraIntent() {
        createTempImageUri();
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);
        startActivityForResult(i, IMAGE_CAPTURE_REQUEST);
    }

    private void launchPickImageIntent() {
        createTempImageUri();
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, IMAGE_PICK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch(requestCode) {
            case IMAGE_CAPTURE_REQUEST:
            break;
            case IMAGE_PICK_REQUEST:
                try {
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    OutputStream out = new FileOutputStream(new File(tempImageUri.getPath()));
                    IOUtils.copy(in, out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        mGiftImage.setTag(tempImageUri);
        // Decode the image in background, off the UI thread.
        new ImageSampler.WorkerURI(mGiftImage).execute(tempImageUri);
    }

    @Override
    public void onGiftChainSelected(BaseRetainedDialog dialogFragment, String tag, GiftChain giftChain) {
        if (giftChain != null) {
            mGiftChain.setText(giftChain.name);
            mGiftChain.setTag(giftChain);
        }
    }

    @Override
    public void onConfirmationFinish(BaseRetainedDialog dialogFragment, String tag, boolean confirmed) {
        if (confirmed) {
            mGiftChain.setTag(null);
        } else {
            mGiftChain.setText(((GiftChain)mGiftChain.getTag()).name);
        }
    }
}
