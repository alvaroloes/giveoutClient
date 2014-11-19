package com.capstone.potlatch;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.capstone.potlatch.base.Config;
import com.capstone.potlatch.base.Routes;
import com.capstone.potlatch.base.State;
import com.capstone.potlatch.net.OAuth2Token;
import com.capstone.potlatch.models.User;
import com.capstone.potlatch.net.JacksonRequest;
import com.capstone.potlatch.net.Net;

import java.util.HashMap;
import java.util.Map;


/**
 * Activities that contain this fragment must implement the
 * {@link com.capstone.potlatch.DialogLogin.OnLoginListener} interface
 * to handle interaction events.
 */
public class DialogLogin extends DialogFragment {
    private static final String tag = "DialogLogin";

    private OnLoginListener mListener;
    private EditText mUsername;
    private EditText mPassword;

    public static DialogLogin show(FragmentManager fragmentManager) {
        DialogLogin dialogLogin = new DialogLogin();
        dialogLogin.show(fragmentManager, tag);
        return dialogLogin;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getString(R.string.login_title));
        View v = inflater.inflate(R.layout.fragment_dialog_login, container, false);

        mUsername = (EditText) v.findViewById(R.id.login_username);
        mPassword = (EditText) v.findViewById(R.id.login_password);

        v.findViewById(R.id.login_accept_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLoginListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnLoginListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null && ! State.get().isUserLoggedIn()) {
            mListener.onLoginCanceled();
        }
    }

    void doLogin() {
        final String username = String.valueOf(mUsername.getText());
        final String password = String.valueOf(mPassword.getText());
        boolean error = false;

        if (username.length() == 0) {
            mUsername.setError("Please enter a user name");
            error = true;
        }

        if (password.length() == 0) {
            mPassword.setError("Please enter a password");
            error = true;
        }

        if (error) {
            return;
        }

        String url = Routes.urlFor(Routes.TOKEN_PATH);

        JacksonRequest<OAuth2Token> req = new JacksonRequest<OAuth2Token>(Request.Method.POST, url,
                                                              OAuth2Token.class,
                                                              new RequestSuccessListener(),
                                                              new RequestErrorListener()) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("grant_type", "password");
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        req.setBasicAuth(Config.basicAuthName, Config.basicAuthPass);

        Net.addToQueue(req);
    }

    class RequestSuccessListener implements Response.Listener<OAuth2Token> {
        @Override
        public void onResponse(OAuth2Token response) {
            State.get().setOauth2Token(response);
            Net.setGlobalOAuth2Token(response);

            if (mListener != null) {
                mListener.onLoginSuccess(State.get().getUser());
                dismiss();
            }
        }
    }

    class RequestErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(getActivity(), "Login error", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnLoginListener {
        public void onLoginSuccess(User user);
        public void onLoginCanceled();
    }

}
