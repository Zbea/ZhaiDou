package com.easemob.easeui.widget.emojicon;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.easemob.easeui.R;
import com.easemob.easeui.adapter.EmojiconGridAdapter;
import com.easemob.easeui.adapter.EmojiconPagerAdapter;
import com.easemob.easeui.domain.EaseEmojicon;
import com.easemob.easeui.domain.EaseEmojicon.Type;
import com.easemob.easeui.domain.EaseEmojiconGroupEntity;
import com.easemob.easeui.utils.EaseSmileUtils;

public class EaseEmojiconPagerView extends ViewPager{

    private Context context;
    private List<EaseEmojiconGroupEntity> groupEntities;
    private List<EaseEmojicon> totalEmojiconList = new ArrayList<EaseEmojicon>();
    
    private PagerAdapter pagerAdapter;
    
    private int emojiconRows = 3;
    private int emojiconColumns = 7;
    
    private int bigEmojiconRows = 2;
    private int bigEmojiconColumns = 4;
    
    private int firstGroupPageSize;
    
    private int maxPageCount;
    private int previousPagerPosition;
	private EaseEmojiconPagerViewListener pagerViewListener;
    private List<View> viewpages; 

    public EaseEmojiconPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public EaseEmojiconPagerView(Context context) {
        this(context, null);
    }
    
    
    public void init(List<EaseEmojiconGroupEntity> emojiconGroupList, int emijiconColumns, int bigEmojiconColumns){
        if(emojiconGroupList == null){
            throw new RuntimeException("emojiconGroupList is null");
        }
        
        this.groupEntities = emojiconGroupList;
        this.emojiconColumns = emijiconColumns;
        this.bigEmojiconColumns = bigEmojiconColumns;
        
        viewpages = new ArrayList<View>();
        for(int i = 0; i < groupEntities.size(); i++){
            EaseEmojiconGroupEntity group = groupEntities.get(i);
            List<EaseEmojicon> groupEmojicons = group.getEmojiconList();
            totalEmojiconList.addAll(groupEmojicons);
            List<View> gridViews = getGroupGridViews(group);
            if(i == 0){
                firstGroupPageSize = gridViews.size();
            }
            maxPageCount = Math.max(gridViews.size(), maxPageCount);
            viewpages.addAll(gridViews);
        }
        
        pagerAdapter = new EmojiconPagerAdapter(viewpages);
        setAdapter(pagerAdapter);
        setOnPageChangeListener(new EmojiPagerChangeListener());
        
        if(pagerViewListener != null){
            pagerViewListener.onPagerViewInited(maxPageCount, firstGroupPageSize);
        }
    }
    
    public void setPagerViewListener(EaseEmojiconPagerViewListener pagerViewListener){
    	this.pagerViewListener = pagerViewListener;
    }
    
    
    /**
     * ???????????????????????????
     * @param position
     */
    public void setGroupPostion(int position){
    	if (getAdapter() != null && position >= 0 && position < groupEntities.size()) {
            int count = 0;
            for (int i = 0; i < position; i++) {
                count += getPageSize(groupEntities.get(i));
            }
            setCurrentItem(count);
        }
    }
    
    /**
     * ??????????????????gridview list
     * @param groupEntity
     * @return
     */
    public List<View> getGroupGridViews(EaseEmojiconGroupEntity groupEntity){
        List<EaseEmojicon> emojiconList = groupEntity.getEmojiconList();
        int itemSize = emojiconColumns * emojiconRows -1;
        int totalSize = emojiconList.size();
        Type emojiType = groupEntity.getType();
        if(emojiType == Type.BIG_EXPRESSION){
            itemSize = bigEmojiconColumns * bigEmojiconRows;
        }
        int pageSize = totalSize % itemSize == 0 ? totalSize/itemSize : totalSize/itemSize + 1;   
        List<View> views = new ArrayList<View>();
        for(int i = 0; i < pageSize; i++){
            View view = View.inflate(context, R.layout.ease_expression_gridview, null);
            GridView gv = (GridView) view.findViewById(R.id.gridview);
            if(emojiType == Type.BIG_EXPRESSION){
                gv.setNumColumns(bigEmojiconColumns);
            }else{
                gv.setNumColumns(emojiconColumns);
            }
            List<EaseEmojicon> list = new ArrayList<EaseEmojicon>();
            if(i != pageSize -1){
                list.addAll(emojiconList.subList(i * itemSize, (i+1) * itemSize));
            }else{
                list.addAll(emojiconList.subList(i * itemSize, totalSize));
            }
            if(emojiType != Type.BIG_EXPRESSION){
                EaseEmojicon deleteIcon = new EaseEmojicon();
                deleteIcon.setEmojiText(EaseSmileUtils.DELETE_KEY);
                list.add(deleteIcon);
            }
            final EmojiconGridAdapter gridAdapter = new EmojiconGridAdapter(context, 1, list, emojiType);
            gv.setAdapter(gridAdapter);
            gv.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    EaseEmojicon emojicon = gridAdapter.getItem(position);
                    if(pagerViewListener != null){
                        String emojiText = emojicon.getEmojiText();
                        if(emojiText != null && emojiText.equals(EaseSmileUtils.DELETE_KEY)){
                            pagerViewListener.onDeleteImageClicked();
                        }else{
                            pagerViewListener.onExpressionClicked(emojicon);
                        }
                        
                    }
                    
                }
            });
            
            views.add(view);
        }
        return views;
    }
    

    /**
     * ???????????????
     * @param groupEntity
     */
    public void addEmojiconGroup(EaseEmojiconGroupEntity groupEntity, boolean notifyDataChange) {
        int pageSize = getPageSize(groupEntity);
        if(pageSize > maxPageCount){
            maxPageCount = pageSize;
            if(pagerViewListener != null && pagerAdapter != null){
                pagerViewListener.onGroupMaxPageSizeChanged(maxPageCount);
            }
        }
        viewpages.addAll(getGroupGridViews(groupEntity));
        if(pagerAdapter != null && notifyDataChange){
            pagerAdapter.notifyDataSetChanged();
        }
    }
    
    /**
     * ???????????????
     * @param position
     */
    public void removeEmojiconGroup(int position){
        if(position > groupEntities.size() - 1){
            return;
        }
        if(pagerAdapter != null){
            pagerAdapter.notifyDataSetChanged();
        }
    }
    
    /**
     * ??????pager??????
     * @param emojiconList
     * @return
     */
    private int getPageSize(EaseEmojiconGroupEntity groupEntity) {
        List<EaseEmojicon> emojiconList = groupEntity.getEmojiconList();
        int itemSize = emojiconColumns * emojiconRows -1;
        int totalSize = emojiconList.size();
        Type emojiType = groupEntity.getType();
        if(emojiType == Type.BIG_EXPRESSION){
            itemSize = bigEmojiconColumns * bigEmojiconRows;
        }
        int pageSize = totalSize % itemSize == 0 ? totalSize/itemSize : totalSize/itemSize + 1;   
        return pageSize;
    }
    
    private class EmojiPagerChangeListener implements OnPageChangeListener{
        @Override
        public void onPageSelected(int position) {
        	int endSize = 0;
        	int groupPosition = 0;
            for(EaseEmojiconGroupEntity groupEntity : groupEntities){
            	int groupPageSize = getPageSize(groupEntity);
            	//?????????position??????????????????group???
            	if(endSize + groupPageSize > position){
            		//?????????group???????????????
            		if(previousPagerPosition - endSize < 0){
            			if(pagerViewListener != null){
            				pagerViewListener.onGroupPositionChanged(groupPosition, groupPageSize);
            				pagerViewListener.onGroupPagePostionChangedTo(0);
            			}
            			break;
            		}
            		//?????????group???????????????
            		if(previousPagerPosition - endSize >= groupPageSize){
            			if(pagerViewListener != null){
            				pagerViewListener.onGroupPositionChanged(groupPosition, groupPageSize);
            				pagerViewListener.onGroupPagePostionChangedTo(position - endSize);
            			}
            			break;
            		}
            		
            		//??????group???pager??????
            		if(pagerViewListener != null){
            			pagerViewListener.onGroupInnerPagePostionChanged(previousPagerPosition-endSize, position-endSize);
            		}
            		break;
            		
            	}
            	groupPosition++;
            	endSize += groupPageSize;
            }
            
            previousPagerPosition = position;
        }
        
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
    }
    
    
    
    public interface EaseEmojiconPagerViewListener{
        /**
         * pagerview???????????????
         * @param groupMaxPageSize ??????????????????page??????
         * @param firstGroupPageSize ????????????page??????
         */
        void onPagerViewInited(int groupMaxPageSize, int firstGroupPageSize);
        
    	/**
    	 * ?????????????????????(?????????????????????????????????)
    	 * @param groupPosition ???????????????
    	 * @param pagerSizeOfGroup ???????????????pager???size
    	 */
    	void onGroupPositionChanged(int groupPosition, int pagerSizeOfGroup);
    	/**
    	 * ???????????????page????????????
    	 * @param oldPosition
    	 * @param newPosition
    	 */
    	void onGroupInnerPagePostionChanged(int oldPosition, int newPosition);
    	
    	/**
    	 * ??????????????????????????????page????????????
    	 * @param position
    	 */
    	void onGroupPagePostionChangedTo(int position);
    	
    	/**
    	 * ???????????????pager?????????
    	 * @param maxCount
    	 */
    	void onGroupMaxPageSizeChanged(int maxCount);
    	
    	void onDeleteImageClicked();
    	void onExpressionClicked(EaseEmojicon emojicon);
    	
    }

}
