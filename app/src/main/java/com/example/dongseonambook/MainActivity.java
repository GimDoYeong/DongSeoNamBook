package com.example.dongseonambook;

import static com.example.dongseonambook.RetrofitClient.getRetrofitInstance;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    EditText userInput;

    TextView resultTv1;


    Button btn;

    ApiServices apiServices;
    ApiServices kakao;

    private RecyclerView recyclerView;
    private RVAdapter adapter;

    String resultSentence;

    View darkOverlay;

    private DatabaseReference databaseRef;

    List<String> resultdlg;

    EditText userInputText;

    Button checkBtn;


    ListView lv;

    private ListViewAdapter lvAdapter;

    ArrayList<String> selectFacName=new ArrayList<>();;

    ArrayList<String> contents=new ArrayList<>();
    ProgressBar loadingSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        lv=findViewById(R.id.lvId);
        lvAdapter=new ListViewAdapter(this,selectFacName);
        lv.setAdapter(lvAdapter);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // ✅ 초기 상태를 COLLAPSED로 설정 (중복 설정 제거)
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

// ✅ 적절한 peekHeight 값 설정 (터치 영역 확보)
        bottomSheetBehavior.setPeekHeight(300);

// ✅ 드래그 활성화
        bottomSheetBehavior.setDraggable(true);

// ✅ 완전히 숨겨지는 것 방지
        bottomSheetBehavior.setHideable(false);

        // ✅ ListView가 터치 이벤트를 BottomSheetBehavior에 전달하도록 설정
        lv.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        });

      lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              // 클릭한 아이템 가져오기
              String clickedItem = contents.get(position);

              // 로그 출력
              Log.d("LIST_CLICK", "클릭한 책: " + clickedItem);

              // 예시: 다이얼로그 띄우기
              new AlertDialog.Builder(MainActivity.this)
                      .setTitle("책 내용")
                      .setMessage(clickedItem)
                      .setPositiveButton("확인", null)
                      .show();
          }
      });


        apiServices = RetrofitClient.getRetrofitInstance().create(ApiServices.class);

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Books");


        userInputText=findViewById(R.id.userInputTextId);
        checkBtn=findViewById(R.id.checkBtn);
        userInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    checkBtn.performClick(); // 버튼 클릭 실행
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(userInputText.getWindowToken(), 0);
                    return true; // 이벤트 소비 (기본 엔터 동작 방지)
                }
                return false;
            }
        });
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String userInputStr=userInputText.getText().toString().trim();
               searchBook(userInputStr);


            }
        });



        resultSentence=null;

        btn=findViewById(R.id.btn);


        List<String> nextQuest = Arrays.asList(
                "애인이 없어",
                "가족과 떨어져 있어",
                "친구가 없어",
                "그냥 자존감 떨어져",
                "무기력해"

        );

        List<String> questions = Arrays.asList(
                "요즘 너무 우울하고 무기력해.",
                "기분이 뭔가 들떠 있어.",
                "모든 게 짜증나고 답답해.",
                "마음이 헛헛하고 외로워.",
                "뭔가 해보고 싶은 에너지가 넘쳐."

        );

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RVAdapter(questions, sentence -> {
            Log.d("FEEL_CLICK", sentence);
            resultSentence=sentence;
            adapter=new RVAdapter(nextQuest,sentence1 -> {
                resultSentence+=sentence1;
            });
            recyclerView.setAdapter(adapter);
        });
        recyclerView.setAdapter(adapter);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FEEL",resultSentence);
                showLoading();
                sendToServer(resultSentence);
            }
        });
    }
    public void sendToServer(String text){
        ApiServices.FeelModel feelModel = new ApiServices.FeelModel(text);
        Call<List<String>> call = apiServices.uploadText(feelModel);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    List<String> feelList = response.body();
                    Log.d("DEBUG", "서버 응답 성공");

                    // ✅ 여기서 서버 응답 사용 X → resultSentence 기준으로 selectedGenre 결정
                    String selectedGenre = "";

                    if (resultSentence.contains("애인이 없어")) {
                        selectedGenre = "연애";
                    } else if (resultSentence.contains("가족과 떨어져 있어")) {
                        selectedGenre = "가족";
                    } else if (resultSentence.contains("친구가 없어")) {
                        selectedGenre = "친구";
                    } else {
                        // 기본 fallback (없으면 "가족"으로 임시 설정)
                        selectedGenre = "가족";
                    }

                    Log.d("DEBUG", "최종 선택된 장르: " + selectedGenre);

                    // ✅ 장르 넘기기
                    fetchBooksFixedAndShowDialog(selectedGenre);
                    hideLoading();
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.d("FEEL", t.getMessage());
                t.printStackTrace();
            }
        });
    }


    private void showLoading() {
        loadingSpinner=findViewById(R.id.loadingSpinner);
        loadingSpinner.setVisibility(View.VISIBLE);
        darkOverlay=findViewById(R.id.dark_overlay);
        darkOverlay.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        loadingSpinner=findViewById(R.id.loadingSpinner);
        loadingSpinner.setVisibility(View.GONE);
        darkOverlay=findViewById(R.id.dark_overlay);
        darkOverlay.setVisibility(View.GONE);
    }

//    public void fetchRandomBooksAndShowSingleDialog() {
//        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Books");
//
//        List<String> genres = new ArrayList<>();
//        genres.add("친구");
//        genres.add("가족");
//        genres.add("연애");
//
//        // 결과 저장할 Map (장르 → 책)
//        Map<String, Map<String, Object>> selectedBooks = new HashMap<>();
//
//        // 비동기 카운트 (3개 다 로드될 때만 다이얼로그 띄우기)
//        final int[] loadedCount = {0};
//
//        for (String genre : genres) {
//            databaseRef.child(genre).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    List<Map<String, Object>> bookList = new ArrayList<>();
//
//                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        Map<String, Object> book = (Map<String, Object>) snapshot.getValue();
//                        if (book != null) {
//                            bookList.add(book);
//                        }
//                    }
//
//                    if (!bookList.isEmpty()) {
//                        Random random = new Random();
//                        Map<String, Object> selectedBook = bookList.get(random.nextInt(bookList.size()));
//
//                        selectedBooks.put(genre, selectedBook);
//                    } else {
//                        Log.d("RANDOM_BOOK_" + genre, "해당 장르에 책이 없습니다.");
//                    }
//
//                    // ✅ 하나 로드 완료 시 카운트 증가
//                    loadedCount[0]++;
//                    if (loadedCount[0] == genres.size()) {
//                        // ✅ 3개 다 로드 완료 → 다이얼로그 띄우기
//                        showMultiBookDialog(selectedBooks);
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    Log.e("RANDOM_BOOK_" + genre, "DB Error: " + databaseError.getMessage());
//                }
//            });
//        }
//    }
public void fetchBooksFixedAndShowDialog(String selectedGenre) {
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Books");

    Map<String, List<String>> fixedKeys = new HashMap<>();
    fixedKeys.put("가족", Arrays.asList("-OReZzDbHE6kZ0PB2xzA", "-OReZzDcK-e4IghBxfP9", "-OReZzDcK-e4IghBxfPA"));
    fixedKeys.put("연애", Arrays.asList("-ORe_53rlkqsglpiDde0", "-OReahP7bDHTuO9FiVG1", "-OReahP80NYPhlinJsZP"));

    List<String> keys = fixedKeys.get(selectedGenre);
    if (keys == null) {
        Log.e("FETCH_BOOKS_FIXED", "선택된 장르 키 없음: " + selectedGenre);
        return;
    }

    List<Map<String, Object>> bookList = new ArrayList<>();
    final int totalBooks = keys.size();
    final int[] loadedCount = {0};

    for (String key : keys) {
        databaseRef.child(selectedGenre).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> book = (Map<String, Object>) dataSnapshot.getValue();
                if (book != null) {
                    bookList.add(book);
                }

                loadedCount[0]++;
                if (loadedCount[0] == totalBooks) {
                    // ✅ 선택한 장르만 다이얼로그로 보여주기
                    Map<String, List<Map<String, Object>>> selectedBooks = new HashMap<>();
                    selectedBooks.put(selectedGenre, bookList);
                    showMultiBookDialog(selectedBooks);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FETCH_BOOKS_FIXED", "DB Error (" + selectedGenre + "): " + databaseError.getMessage());
            }
        });
    }
}


    private void showMultiBookDialog(Map<String, List<Map<String, Object>>> selectedBooks) {
        // ✅ 다이얼로그 안에 들어갈 layout 만들기
        LinearLayout bookContainer = new LinearLayout(this);
        bookContainer.setOrientation(LinearLayout.VERTICAL);
        bookContainer.setPadding(32, 32, 32, 32);

        // ✅ 이미지 이름 배열 (장르별)
        String[] loveNames = {"love1", "love2", "love3"};
        String[] famNames = {"fam1", "fam2", "fam3"};

        // ✅ 장르별로 반복
        for (String genre : selectedBooks.keySet()) {
            List<Map<String, Object>> bookList = selectedBooks.get(genre);

            // ✅ 섹션 헤더 (장르)
            TextView header = new TextView(this);
            header.setText("📚 [" + genre + "] 추천 도서");
            header.setTextSize(20);
            header.setPadding(0, 16, 0, 16);
            header.setTextColor(getResources().getColor(android.R.color.black));
            bookContainer.addView(header);

            // ✅ 책 3권 반복
            for (int i = 0; i < bookList.size(); i++) {
                Map<String, Object> book = bookList.get(i);

                // ✅ 각 책 아이템 레이아웃 (가로)
                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(0, 8, 0, 8);

                // ✅ 이미지뷰 생성
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(200, 300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                // ✅ 장르별 이미지 선택
                String imageName = "defaultbtn";
                if (genre.equals("연애")) {
                    imageName = loveNames[i];
                } else if (genre.equals("가족")) {
                    imageName = famNames[i];
                }

                int imageResId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                if (imageResId != 0) {
                    imageView.setImageResource(imageResId);
                } else {
                    imageView.setImageResource(R.drawable.defaultbtn);
                }

                // ✅ 텍스트뷰 생성 (책 정보)
                TextView textView = new TextView(this);
                textView.setPadding(16, 0, 0, 0);
                textView.setTextSize(16);
                textView.setText("제목: " + book.get("title") + "\n"
                        + "저자: " + book.get("author") + "\n"
                        + "출판사: " + book.get("publisher") + "\n"
                        + "줄거리: " + book.get("summary") + "\n");

                // ✅ itemLayout 에 이미지 + 텍스트 추가
                itemLayout.addView(imageView);
                itemLayout.addView(textView);

                // ✅ bookContainer 에 itemLayout 추가
                bookContainer.addView(itemLayout);
            }
        }

        // ✅ AlertDialog 생성
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("오늘의 추천 도서 (장르별 3권씩)")
                .setView(bookContainer) // ✅ 커스텀 뷰 설정
                .setPositiveButton("확인", null)
                .show();
    }


    private void searchBook(String keyword) {
        kakao=apiServices = RetrofitClientKakao.getRetrofitInstance().create(ApiServices.class);

        String apiKey = "";  // 주의: "KakaoAK " 포함해야 함!


        Call<ApiServices.KakaoBookResponse> call = apiServices.searchBooks(apiKey, keyword, 1, 10);

        call.enqueue(new retrofit2.Callback<ApiServices.KakaoBookResponse>() {

            @Override
            public void onResponse(Call<ApiServices.KakaoBookResponse> call, Response<ApiServices.KakaoBookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ApiServices.KakaoBookResponse.Document> books = response.body().documents;
                    selectFacName.clear();
                    contents.clear();
                    for (ApiServices.KakaoBookResponse.Document book : books) {
                        selectFacName.add(book.title+" "+book.authors);
                        contents.add(book.contents);
                    }
                    lvAdapter.notifyDataSetChanged();


                    // 필요하면 → 다이얼로그로 표시 가능
                    // 또는 RecyclerView로 목록 보여주기 가능
                } else {
                    Log.e("KAKAO_BOOK", "응답 실패: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiServices.KakaoBookResponse> call, Throwable t) {
                Log.e("KAKAO_BOOK", "에러 발생: " + t.getMessage());
            }
        });
    }

}