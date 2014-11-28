package com.capstone.potlatch.dialogs;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.capstone.potlatch.R;
import com.capstone.potlatch.base.Config;
import com.capstone.potlatch.base.Routes;
import com.capstone.potlatch.base.State;
import com.capstone.potlatch.models.User;
import com.capstone.potlatch.net.Net;
import com.capstone.potlatch.net.OAuth2Token;
import com.capstone.potlatch.net.requests.AuthRequest;
import com.capstone.potlatch.net.requests.OAuth2TokenRequest;


/**
 * Activities that contain this fragment must implement the
 * {@link DialogLogin.OnLoginListener} interface
 * to handle interaction events.
 */
public class DialogLogin extends BaseRetainedDialog {
    private OnLoginListener mListener;

    class UI {
        private EditText username;
        private EditText password;
    }

    private UI ui;

    public static DialogLogin open(FragmentManager fragmentManager, String tag) {
        DialogLogin dialogLogin = (DialogLogin) fragmentManager.findFragmentByTag(tag);
        if (dialogLogin == null) {
            dialogLogin = new DialogLogin();
            dialogLogin.show(fragmentManager, tag);
        }
        return dialogLogin;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ui = new UI();
        getDialog().setTitle(getActivity().getString(R.string.login_title));
        View v = inflater.inflate(R.layout.fragment_dialog_login, container, false);

        ui.username = (EditText) v.findViewById(R.id.login_username);
        ui.password = (EditText) v.findViewById(R.id.login_password);

        v.findViewById(R.id.login_accept_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        ui = null;
        super.onDestroyView();
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
            mListener.onLoginFinish(this, getTag(), false);
        }
    }

    void doLogin() {
        final String username = String.valueOf(ui.username.getText());
        final String password = String.valueOf(ui.password.getText());
        boolean error = false;

        if (username.length() == 0) {
            ui.username.setError("Please enter a user name");
            error = true;
        }

        if (password.length() == 0) {
            ui.password.setError("Please enter a password");
            error = true;
        }

        if (error) {
            return;
        }

        String url = Routes.urlFor(Routes.TOKEN_PATH);
        OAuth2TokenRequest req = new OAuth2TokenRequest(url, username, password, new RequestLoginSuccessListener(), new RequestErrorListener());
        req.setBasicAuth(Config.basicAuthName, Config.basicAuthPass);
        Net.addToQueue(req);
    }

    class RequestLoginSuccessListener implements Response.Listener<OAuth2Token> {
        @Override
        public void onResponse(OAuth2Token response) {
            State.get().setOauth2Token(response);
            Net.setGlobalOAuth2Token(response);

            // Get user data
            String url = Routes.urlFor(Routes.CURRENT_USER_PATH);
            AuthRequest<User> req = new AuthRequest<User>(Request.Method.GET, url, User.class, new Response.Listener<User>() {
                @Override
                public void onResponse(User response) {
                    State.get().setUser(response);
                    if (mListener != null) {
                        mListener.onLoginFinish(DialogLogin.this, getTag(), true);
                        dismiss();
                    }
                }
            }, new RequestErrorListener());
            Net.addToQueue(req);
        }
    }

    class RequestErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(getActivity(), "Login error", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnLoginListener {
        public void onLoginFinish(BaseRetainedDialog dialogFragment, String tag, boolean success);
    }

}
