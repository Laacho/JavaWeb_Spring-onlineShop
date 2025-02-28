package app.cloudinary;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }


    public String uploadFromUrlToProducts(String imageUrl) throws IOException {
        Map<String, Object> options  = new HashMap<>();
        options.put("folder","products");
        Map uploadResult = cloudinary.uploader().upload(imageUrl, options);
        return uploadResult.get("url").toString();
        // Returns the image URL
    }
    public String uploadFromUrlToUserProfiles(String imageUrl) throws IOException {
        Map<String, Object> options  = new HashMap<>();
        options.put("folder","user_profile");
        Map uploadResult = cloudinary.uploader().upload(imageUrl, options);
        return uploadResult.get("url").toString();
    }
}
