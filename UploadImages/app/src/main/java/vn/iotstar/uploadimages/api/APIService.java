package vn.iotstar.uploadimages.api;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Part;
import vn.iotstar.uploadimages.model.ImageUpload;
import vn.iotstar.uploadimages.utils.Const;

public interface APIService {
    @POST("upload.php")
    Call<ImageUpload> upload (@Part(Const.MY_USERNAME)RequestBody username, @Part MultipartBody.Part avatar);
}
