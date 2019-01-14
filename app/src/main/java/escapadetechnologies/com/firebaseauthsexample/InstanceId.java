package escapadetechnologies.com.firebaseauthsexample;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class InstanceId extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        try {

            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            Log.e("refreshedToken", refreshedToken);
        }catch (Exception e)
        {
            Log.e("catchccc",e.getMessage());
        }
    }
}
