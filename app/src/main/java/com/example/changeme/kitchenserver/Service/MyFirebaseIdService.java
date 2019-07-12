package com.example.changeme.kitchenserver.Service;

import com.example.changeme.kitchenserver.Common.Common;
import com.example.changeme.kitchenserver.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by SHEGZ on 1/4/2018.
 */
public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        super.onTokenRefresh();
        String tokenrefreshed = FirebaseInstanceId.getInstance().getToken();
        if (Common.currentuser != null)
            updateTokenToFirebase(tokenrefreshed);

    }

    private void updateTokenToFirebase(String tokenrefreshed) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token token = new Token(tokenrefreshed, true);
        tokens.child(Common.currentuser.getPhone()).setValue(token);


    }
}
