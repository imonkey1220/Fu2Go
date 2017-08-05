package tw.imonkey.fu2go;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

class rvDeviceHolder extends RecyclerView.ViewHolder  {
    private final TextView mPinField;
    private final TextView mPinStateField;
    private final Switch mPinTypeField;
    private final TextView mTimeStampField ;
    private final ImageView mPhotoField;
    public rvDeviceHolder(View itemView) {
        super(itemView);
        mPinField = (TextView) itemView.findViewById(R.id.textViewPin);
        mPinStateField=(TextView) itemView.findViewById(R.id.textViewPinState);
        mTimeStampField=(TextView)itemView.findViewById(R.id.textViewPinTimeStamp);
        mPinTypeField=(Switch)itemView.findViewById(R.id.switchPin);
        mPhotoField =(ImageView) itemView.findViewById(R.id.imageViewPin);
    }

    void setPin(String pin) {
        mPinField.setText(pin);
    }
    void setPinState(String pinState) {
        mPinStateField.setText(pinState);
    }
    void setPinType(boolean isSwitch) {
        if (isSwitch){
            mPinTypeField.setVisibility(View.VISIBLE);
        }else{
            mPinTypeField.setVisibility(View.INVISIBLE);
        }
    }
    void setTimeStamp(String timeStamp) {
        mTimeStampField.setText(timeStamp);
    }
    void setPhoto(String pinId) {
        String devicePhotoPath = "/devicePhoto/" +pinId;
        StorageReference mImageRef = FirebaseStorage.getInstance().getReference(devicePhotoPath);
        Glide.with(mPhotoField.getContext())
                .using(new FirebaseImageLoader())
                .load(mImageRef)
                .into(mPhotoField);
    }
}
