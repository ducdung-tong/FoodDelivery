package com.example.fooddelivery.model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fooddelivery.R;
import com.example.fooddelivery.activity.NotificationActivity;
import com.example.fooddelivery.adapter.NotificationAdapter;
import com.example.fooddelivery.fragment.NotificationFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ModifyFirebase {
    private static final String TAG = "firebaseFirstore";
    private Object object;
    private String docRef;
    private String userId;
    private Uri[] image;
    private String collectionPath = "";
    public ArrayList<MyNotification> notifications = new ArrayList<>();
    public ArrayList<SearchString> searchList = new ArrayList<>();
    public ArrayList<Product> cartList = new ArrayList<>();
    public ArrayList<Product> productList = new ArrayList<Product>();
    public ArrayList<String> watchedList = new ArrayList<>();
    public ArrayList<String> favouriteProductList = new ArrayList<String>();
    public ArrayList<Merchant> merchantList = new ArrayList<Merchant>();
    public ArrayList<Voucher> voucherList = new ArrayList<Voucher>();
    public ArrayList<Voucher> availableVoucherList = new ArrayList<Voucher>();
    private FirebaseFirestore root = FirebaseFirestore.getInstance();
    private StorageReference reference = FirebaseStorage.getInstance().getReference();
    private final boolean checkUsername = false;
    private boolean uIDCheck = false;
    private User user = new User();

    public ModifyFirebase() {
    }

    public void addProductToFavourite(Context context, String productId) {
        Map<String, String> product = new HashMap<>();
        product.put("ProductId", productId);
        root.collection("User/" + userId + "/Favourite/")
                .document(productId)
                .set(product)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, context.getString(R.string.add_favourite_success), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void removeProductFromFavourite(Context context, String productId) {
        root.collection("User/" + userId + "/Favourite/")
                .document(productId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, context.getString(R.string.remove_favourite_success), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    public void addProductToCart(String productId) {

    }

    public void addProductToWatched(String productId) {
        Map<String, String> product = new HashMap<>();
        product.put("ProductId", productId);
        root.collection("User/" + userId + "/Watched/")
                .document(productId)
                .set(product)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
    }

    public void removeWatchedProductData(final OnGetDataListener listener) {
        listener.onStart();
        for (String productId : watchedList) {
            root.collection("User/" + userId + "/Watched/")
                    .document(productId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            watchedList.remove(productId);
                        }
                    });
        }
        listener.onSuccess();
    }

    public void getNotificationList(final OnGetDataListener listener) {
        listener.onStart();
        root.collection("User/" + userId + "/Notification")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null)
                        {
                            Log.e(TAG, "onEvent------------------------", error);
                        }
                        if (value != null)
                        {
                            List<DocumentChange> snapshotList = value.getDocumentChanges();

                            for (DocumentChange snapshot : snapshotList)
                            {
                                for (MyNotification noti : notifications)
                                {
                                    if (snapshot.getDocument().getId() == noti.getId()) {
                                        notifications.remove(noti);
                                        break;
                                    }
                                }

                                MyNotification notification = new MyNotification();
                                notification.setId(snapshot.getDocument().getId());
                                notification.setTitle(snapshot.getDocument().get("Title").toString());
                                notification.setDesc(snapshot.getDocument().get("Detail").toString());
                                notification.setTime(snapshot.getDocument().get("Date").toString());
                                notification.setStatus(snapshot.getDocument().get("Status").toString());

                                notifications.add(notification);
                            }

                            Collections.reverse(notifications);

                            listener.onSuccess();

                            if (NotificationFragment.getAdapter() != null)
                            {
                                NotificationFragment.getAdapter().notifyDataSetChanged();
                            }
                        } else {
                            Log.e(TAG, "onEvent querry was null-----------------");
                        }
                    }
                });

    }

    public void updateNotificationStatus(String id)
    {
        root.collection("User/" + userId + "/Notification/")
                .document(id)
                .update("Status", "true");
    }

    public void getWatchedProductList(final OnGetDataListener listener) {
        listener.onStart();
        root.collection("User/" + userId + "/Watched")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (document == null)
                                break;
                            watchedList.add((String) document.get("ProductId"));
                        }
                        listener.onSuccess();
                    }
                });
    }

    public void getVoucherList() {
        root.collection("User/" + userId + "/Voucher/")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d("GOT" , document.getId());
                            Voucher voucher = new Voucher();
                            voucher.setStatus(document.get("status").toString());
                            voucher.setId(document.getId());
                            root.collection("Voucher/")
                                    .document(document.getId())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            voucher.setCode(documentSnapshot.get("code").toString());
                                            voucher.setTitle(documentSnapshot.get("title").toString());
                                            voucher.setDate(documentSnapshot.get("date").toString());
                                            voucher.setValues((List<String>) documentSnapshot.get("value"));
                                            voucher.setDetails((List<String>) documentSnapshot.get("details"));
                                            voucherList.add(voucher);
                                        }
                                    });
                        }
                    }
                });
    }

    public void getAvailableVoucherList() {
        root.collection("Voucher/")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Voucher voucher = new Voucher();
                            voucher.setId(document.getId());
                            voucher.setCode(document.get("code").toString());
                            voucher.setTitle(document.get("title").toString());
                            voucher.setDate(document.get("date").toString());
                            voucher.setValues((List<String>) document.get("value"));
                            voucher.setDetails((List<String>) document.get("details"));
                            availableVoucherList.add(voucher);
                        }
                    }
                });
    }

    public void getComment() {
        int index = 0;
        for (Product product : productList) {
            ArrayList<Comment> comments = new ArrayList<Comment>();
            int finalIndex = index;
            root.collection("Product/" + product.getId() + "/Comment/")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            Log.d(TAG, "Got comment from product");
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                if (document == null)
                                    break;
                                Comment comment = new Comment();
                                comment.setiD(document.getId());
                                comment.setUserName(document.get("userName").toString());
                                comment.setDate(document.get("date").toString());
                                comment.setDetails(document.get("details").toString());
                                comment.setRating(document.get("rating").toString());
                                comments.add(comment);
                            }
                            productList.get(finalIndex).setCommentList(comments);
                        }
                    });
            index++;
        }
    }

    public void getData(final OnGetDataListener listener) {
        listener.onStart();
        //Load merchant list
        root.collection("Merchant/")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (document == null)
                                break;
                            Merchant merchant = new Merchant();
                            merchant.setName((String) document.get("Name"));
                            merchant.setAddress((String) document.get("Address"));
                            merchant.setId((String) document.getId());

                            ArrayList<Uri> merchantImages = new ArrayList<Uri>();
                            root.collection("Merchant/" + merchant.getId() + "/Photos/")
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                if (document == null)
                                                    break;
                                                if (((String) document.get("Image_Link")) != null) {
                                                    merchantImages.add(Uri.parse((String) document.get("Image_Link")));
                                                    break;
                                                }
                                            }
                                            merchant.setImage(merchantImages);
                                            merchantList.add(merchant);
                                        }
                                    });
                        }

                        //Load Product list
                        root.collection("Product/")
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                            if (document == null)
                                                break;
                                            Product product = new Product();
                                            product.setId((String) document.getId());
                                            product.setName((String) document.get("Name"));
                                            product.setEn_Name((String) document.get("Name_En"));
                                            product.setStatus((String) document.get("Status"));
                                            product.setPrice((ArrayList<String>) document.get("Price"));
                                            product.setProductSize((ArrayList<String>) document.get("Size"));
//                                            product.setMerchant((Merchant) findMerchantFromId(((String) document.get("Merchant")).substring(9)));
                                            product.setRating((String) document.get("Rating"));
                                            product.setType((String) document.get("Type"));

                                            //Load product images
                                            ArrayList<Uri> images = new ArrayList<Uri>();
                                            root.collection("Product/" + product.getId() + "/Photos/")
                                                    .get()
                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                                if (document == null)
                                                                    break;
                                                                if (((String) document.get("Image_Link")) != null) {
                                                                    images.add(Uri.parse((String) document.get("Image_Link")));
                                                                    break;
                                                                }
                                                            }
                                                            product.setImage(images);
                                                            product.setMerchant((Merchant) findMerchantFromId(((String) document.get("Merchant")).substring(9)));
                                                            productList.add(product);
                                                            if (productList.size() > 1 && merchantList.size() > 1)
                                                                listener.onSuccess();
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                });


        root.collection("User/" + userId + "/Favourite")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (document == null)
                                break;
                            favouriteProductList.add((String) document.get("ProductId"));
                        }
                    }
                });

        //Load product in cart
    }

    public void loadFullListMerchantImage(Merchant merchant, final OnGetDataListener listener) {
        listener.onStart();
        ArrayList<Uri> merchantImages = new ArrayList<Uri>();
        root.collection("Merchant/" + merchant.getId() + "/Photos/")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (document == null)
                                break;
                            if (((String) document.get("Image_Link")) != null) {
                                merchantImages.add(Uri.parse((String) document.get("Image_Link")));
                            }
                        }
                        merchant.setImage(merchantImages);
                        listener.onSuccess();
                    }
                });
    }

    public void getSearchData(final OnGetDataListener listener) {
        listener.onStart();
        root.collection("User/" + userId + "/Search/")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (document == null)
                                break;
                            SearchString data = new SearchString();
                            data.setId((String) document.getId());
                            data.setDetail((String) document.get("SearchString"));
                            searchList.add(data);
                        }
                        listener.onSuccess();
                    }
                });
    }

    public void removeSearchData(final OnGetDataListener listener) {
        listener.onStart();
        for (SearchString search : searchList) {
            root.collection("User/" + userId + "/Search/")
                    .document(search.getId())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            searchList.remove(search);
                        }
                    });
        }
        listener.onSuccess();
    }

    public void insertDataFirestore(String id) {
        root.collection(collectionPath).document(id).set(object)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        docRef = id;
                    }
                });
    }

    private Merchant findMerchantFromId(String id) {
        for (Merchant mer : merchantList) {
            if (mer.getId().equals(id))
                return mer;
        }
        return null;
    }

    public void getUserInfo() {
        root.collection("User").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                user.Email = document.get("email").toString();
                                user.Address = document.get("address").toString();
                                user.First_Name = document.get("first_Name").toString();
                                user.Last_Name = document.get("last_Name").toString();
                                user.Phone_Number = document.get("phone_Number").toString();

                                StorageReference fileRef = reference.child("UserImage/"+ userId);
                                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        user.setProfileImage(uri);
                                    }
                                });
                            }
                        }
                    }
                });
    }

    public void addVoucherToList(Context context, String voucherId) {
        Map<String, String> voucher = new HashMap<>();
        voucher.put("status", "Hiện có");
        root.collection("User/" + userId + "/Voucher/")
                .document(voucherId)
                .set(voucher)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Thêm voucher thành công, vào ví để kiểm tra!", Toast.LENGTH_SHORT);
                    }
                });
    }

    public void addNewUser(User user, String uid) {
        Map<String, String> u = new HashMap<>();
        u.put("first_Name", user.getFirst_Name());
        u.put("last_Name", user.getLast_Name());
        u.put("address", user.getAddress());
        u.put("phone_Number", user.getPhone_Number());
        u.put("email", user.getEmail());
        root.collection("User")
                .document(uid)
                .set(u)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
    }

    public boolean checkUID(String uID) {
        uIDCheck = false;
        root.collection("User").document(uID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        uIDCheck = true;
                        documentSnapshot.get("name");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        return uIDCheck;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getDocRef() {
        return docRef;
    }

    public void setDocRef(String docRef) {
        this.docRef = docRef;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Uri[] getImage() {
        return image;
    }

    public void setImage(Uri[] image) {
        this.image = image;
    }

    public String getCollectionPath() {
        return collectionPath;
    }

    public void setCollectionPath(String collectionPath) {
        this.collectionPath = collectionPath;
    }

    public FirebaseFirestore getRoot() {
        return root;
    }

    public void setRoot(FirebaseFirestore root) {
        this.root = root;
    }

    public StorageReference getReference() {
        return reference;
    }

    public void setReference(StorageReference reference) {
        this.reference = reference;
    }

    public boolean isCheckUsername() {
        return checkUsername;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
