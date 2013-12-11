package com.cubes.learningcubes;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchResultsListAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Lesson> values;
    private LayoutInflater inflater;
    private Random random;

    public SearchResultsListAdapter(Context context, ArrayList<Lesson> values) {
            
    //call the super class constructor and provide the ID of the resource to use instead of the default list view item
      this.context = context;
      this.values = values;
      random = new Random();
      inflater = LayoutInflater.from(context); 
    }
    
    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public long getItemId (int position) {
        return position;
    }

    @Override
    public Object getItem (int position) {
        return values.get(position);
    }
    
    public void addItem(Lesson item) {
    	values.add(item);
    	this.notifyDataSetChanged();
    }
    //this method is called once for each item in the list
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
      View listItem = inflater.inflate(R.layout.lesson_web_list_item, parent, false);
      Lesson lesson = values.get(position);
      
      TextView lessonName = (TextView)listItem.findViewById(R.id.web_lesson_name);
      lessonName.setText(lesson.lessonName);
      
      TextView lessonDescription = (TextView)listItem.findViewById(R.id.lesson_description);
      lessonDescription.setText(values.get(position).description);

      TextView lessonPrice = (TextView)listItem.findViewById(R.id.lesson_price);
      lessonPrice.setText(values.get(position).getPrice());
      
      listItem.setTag(lesson.remoteId);
      
      int starValue = random.nextInt(5);
      int resId = 0;
      switch(starValue) {
      	case 1:
      		resId = R.drawable.one_star;
      		break;
      	case 2:
      		resId = R.drawable.two_stars;
      		break;
      	case 3:
      		resId = R.drawable.three_stars;
      		break;
      	case 4: 
        default:
      		resId = R.drawable.four_stars;
      		break;
      }
      
      ImageView image = (ImageView)listItem.findViewById(R.id.star_rating);
      image.setImageDrawable(context.getResources().getDrawable(resId));
      
      
      int numDownloads = random.nextInt(900);
      TextView numberDownloads = (TextView)listItem.findViewById(R.id.number_downloads);
      numberDownloads.setText(numDownloads + " downloads");

      return listItem;
    
    }

}