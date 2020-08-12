package com.example.blogappdjangorest.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.blogappdjangorest.Adapter.GroupsAdapter;
import com.example.blogappdjangorest.Models.RetrofitModels.AddBlogResponse;
import com.example.blogappdjangorest.Models.RetrofitModels.CategoryResponse;
import com.example.blogappdjangorest.Models.RetrofitModels.GroupListResponse;
import com.example.blogappdjangorest.R;
import com.example.blogappdjangorest.Retrofit.ApiClient;
import com.example.blogappdjangorest.resources.PreferencesHelper;
import com.example.blogappdjangorest.resources.WaitingDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class AddBlogFragment extends Fragment {

    MaterialButton button,category,add_image;
    String[] value=new String[22];
    ApiClient apiClient;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    EditText blog_title,body;
    String category_text,status,group_text;
    String[] group,group_id;
    Uri uri;
    CircleImageView image;
    StorageReference folder;
    WaitingDialog waitingDialog;
    TextView header_title;
    PreferencesHelper preferencesHelper;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_addblog,container,false);

        button=view.findViewById(R.id.next_button);
        category=view.findViewById(R.id.cat_button);
        blog_title=view.findViewById(R.id.blog_title);
        body=view.findViewById(R.id.body);
        add_image=view.findViewById(R.id.add_image);
        image=view.findViewById(R.id.image);
        apiClient=new ApiClient();
        waitingDialog=new WaitingDialog(getContext());
        category.setEnabled(false);
        header_title=view.findViewById(R.id.title);
        header_title.setText("Add Blog");
        preferencesHelper = new PreferencesHelper(getContext());
        get_cat();
        get_group();

        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDialog();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showpublishDialog();
            }
        });

        add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });

        return view;
    }

    public void showpublishDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Publish");
        String[] animals = {"public", "Private (In Group)"};
        status="public";
        builder.setSingleChoiceItems(animals, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which==0)
                {
                    status="public";
                }
                else
                {
                    status="private";
                }

            }
        });


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (status.equals("public")) {
                    group_text="1";
                    upload();
                    dialog.dismiss();
                } else {
                    if (group.length==0)
                    {
                        Toast.makeText(getContext(),"No group to show, please Create the group or publish it public",Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        status="private";
                        showGroupDialog();
                        dialog.dismiss();
                        Log.e("value",status);

                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showGroupDialog()
    {
        Log.e("value",status);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select the group");
        group_text=group_id[0];
        builder.setSingleChoiceItems(group, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                group_text=group_id[which];
                Log.e("value",status);
            }
        });


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Log.e("value",status);
                upload();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void get_cat() {
        Call<ArrayList<CategoryResponse>> call = apiClient.getApiinterface().get_categories();

        call.enqueue(new Callback<ArrayList<CategoryResponse>>() {
            @Override
            public void onResponse(Call<ArrayList<CategoryResponse>> call, Response<ArrayList<CategoryResponse>> response) {

                if (response.code() == 200) {
                    if (!(response.body().size() == 0)) {
                        category.setEnabled(true);
                        for (int a = 0; a < response.body().size(); a++) {
                            value[a]=(response.body().get(a).getCategory_name());
                        }
                    } else {
                        category.setText("Something Went Wrong");
                        category.setEnabled(false);
                    }
                    build_dialog();

                }

            }

            @Override
            public void onFailure(Call<ArrayList<CategoryResponse>> call, Throwable t) {

            }
        });
    }
    private void build_dialog()
    {
        category_text="Technology";
        category.setText("Technology");
        builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Search By:");
        builder.setSingleChoiceItems(value, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                category.setText(value[which]);
                category_text=value[which];
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton("Cancel", null);
        dialog = builder.create();
    }

    public void showDialog() {

        dialog.show();
    }
    private void add_blog(Uri uri)
    {

        Log.e("value",status);
        waitingDialog.setext("Saving the data...");
        Call<AddBlogResponse> call=apiClient.getApiinterface().add_blog(String.valueOf(uri),blog_title.getText().toString(),body.getText().toString(),category_text,preferencesHelper.getid(),status,group_text);

        call.enqueue(new Callback<AddBlogResponse>() {
            @Override
            public void onResponse(Call<AddBlogResponse> call, Response<AddBlogResponse> response) {

                if (response.code()==200)
                {
                    if (!response.body().toString().isEmpty())
                    {
                        Toast.makeText(getContext(),"Successfully Added",Toast.LENGTH_LONG).show();
                        Log.e("value",status);
                        waitingDialog.dismiss();

                    }
                    else
                        Log.e("ok","q");
                }
                else
                Log.e("ok","r");
            }

            @Override
            public void onFailure(Call<AddBlogResponse> call, Throwable t) {

                Log.e("ok","w");
            }
        });
    }

    private void check()
    {

    }

    private void get_group()
    {
        Call<ArrayList<GroupListResponse>> call=apiClient.getApiinterface().get_group("10");
        call.enqueue(new Callback<ArrayList<GroupListResponse>>() {
            @Override
            public void onResponse(Call<ArrayList<GroupListResponse>> call, Response<ArrayList<GroupListResponse>> response) {

                if (response.code()==200)
                {
                    if (!(response.body().size() ==0))
                    {
                        group=new String[response.body().size()];
                        group_id=new String[response.body().size()];
                        for (int a=0;a<response.body().size();a++)
                        {
                            group[a]=response.body().get(a).getGroup_description();
                            group_id[a]=response.body().get(a).getGroup_id();
                        }
                    }
                }

            }

            @Override
            public void onFailure(Call<ArrayList<GroupListResponse>> call, Throwable t) {

            }
        });
    }

    private void upload()
    {
        Log.e("value",status);
        folder= FirebaseStorage.getInstance().getReference().child("ImageFolder");
        final StorageReference image_store = folder.child("image" + uri.getLastPathSegment());
        UploadTask uploadTask=image_store.putFile(uri);
        waitingDialog.SetDialog("Uploading Your\nfile...");
        waitingDialog.show();

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress=(100 * taskSnapshot.getBytesTransferred()) /taskSnapshot.getTotalByteCount();
                waitingDialog.setext((int) progress +" % completed");
            }
        });

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                image_store.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        add_blog(uri);
                        Log.e("value",status);
                    }
                });
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            uri=data.getData();
            image.setImageURI(uri);
            add_image.setText("Edit Image");
        }
    }
}
