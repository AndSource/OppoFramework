package android.nfc;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.IOException;

public final class TransceiveResult implements Parcelable {
    public static final Parcelable.Creator<TransceiveResult> CREATOR = new Parcelable.Creator<TransceiveResult>() {
        /* class android.nfc.TransceiveResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TransceiveResult createFromParcel(Parcel in) {
            byte[] responseData;
            int result = in.readInt();
            if (result == 0) {
                responseData = new byte[in.readInt()];
                in.readByteArray(responseData);
            } else {
                responseData = null;
            }
            return new TransceiveResult(result, responseData);
        }

        @Override // android.os.Parcelable.Creator
        public TransceiveResult[] newArray(int size) {
            return new TransceiveResult[size];
        }
    };
    public static final int RESULT_EXCEEDED_LENGTH = 3;
    public static final int RESULT_FAILURE = 1;
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_TAGLOST = 2;
    final byte[] mResponseData;
    final int mResult;

    public TransceiveResult(int result, byte[] data) {
        this.mResult = result;
        this.mResponseData = data;
    }

    public byte[] getResponseOrThrow() throws IOException {
        int i = this.mResult;
        if (i == 0) {
            return this.mResponseData;
        }
        if (i == 2) {
            throw new TagLostException("Tag was lost.");
        } else if (i != 3) {
            throw new IOException("Transceive failed");
        } else {
            throw new IOException("Transceive length exceeds supported maximum");
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mResult);
        if (this.mResult == 0) {
            dest.writeInt(this.mResponseData.length);
            dest.writeByteArray(this.mResponseData);
        }
    }
}
