package com.example.dongseonambook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    EditText email, password, pwdCheck;
    ProgressBar loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase Auth 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();
        // Firebase Realtime Database 참조 초기화
        databaseRef = FirebaseDatabase.getInstance().getReference();

        email = findViewById(R.id.email);
        password = findViewById(R.id.pwd);
        pwdCheck = findViewById(R.id.pwdCheck);
        Button finBtn = findViewById(R.id.signBtn);

        finBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strEmail = email.getText().toString().trim();
                String strPwd = password.getText().toString().trim();
                String strPwdCheck = pwdCheck.getText().toString().trim();

                if (strEmail.isEmpty()) {
                    email.setError("이메일을 입력해주세요");
                    email.requestFocus();
                    return;
                }
                if (!strEmail.contains("@")) {
                    email.setError("이메일의 형식이 아닙니다.");
                    email.requestFocus();
                    return;
                }
                if (strPwd.isEmpty()) {
                    password.setError("비밀번호를 입력해주세요");
                    password.requestFocus();
                    return;
                }
                if (strPwd.length() < 6) {
                    password.setError("비밀번호는 최소 6자리 이상이어야 합니다");
                    password.requestFocus();
                    return;
                }
                if (!(strPwd.equals(strPwdCheck))) {
                    pwdCheck.setError("입력한 비밀번호가 일치하지 않습니다.");
                    pwdCheck.requestFocus();
                    return;
                }

                // Firebase Auth로 사용자 생성
                mAuth.createUserWithEmailAndPassword(strEmail, strPwd)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // 계정 생성 성공
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String uid = user.getUid();

                                    // 사용자 정보를 Realtime Database에 저장
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("email", strEmail);

                                    databaseRef.child("users").child(uid).setValue(userMap)
                                            .addOnSuccessListener(aVoid -> {
                                                // 저장 성공 시 처리
                                                System.out.println("성공");
                                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                System.out.println("실패: " + e.getMessage());
                                            });

                                } else {
                                    Exception exception = task.getException();
                                    if (exception instanceof FirebaseAuthUserCollisionException) {
                                        email.setError("중복된 이메일입니다!");
                                        email.requestFocus();
                                    }
                                }
                            }
                        });
            }
        });
    }
}
