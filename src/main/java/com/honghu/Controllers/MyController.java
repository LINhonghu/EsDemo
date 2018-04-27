package com.honghu.Controllers;



import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by 鸿鹄 on 2018/4/26.
 */
@RestController
public class MyController {
    @Autowired
    private TransportClient client;
    @GetMapping("/get/books/novel")
    @ResponseBody
    public ResponseEntity get(@RequestParam(name="id",defaultValue = "") String id){
        //预执行
        GetResponse response= this.client.prepareGet("books","novel",id).get();
        if(id.isEmpty()){
            return new ResponseEntity(response.getSource(), HttpStatus.NOT_FOUND);
        }
        if(!response.isExists()){
            return new ResponseEntity(response.getSource(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(response.getSource(), HttpStatus.OK);
    }
    @PostMapping("/add/books/novel")
    @ResponseBody
    public ResponseEntity add(
            @RequestParam(name="title") String title,
            @RequestParam(name="author")String author,
            @RequestParam(name="word_count") int wordCount,
            @RequestParam(name="publish_date")
             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date publishDate
    ) {
        try {
            XContentBuilder content= XContentFactory.jsonBuilder()
                    .startObject()
                    .field("title",title)
                    .field("author",author)
                    .field("word_count",wordCount)
                    .field("publish_date",publishDate.getTime())
                    .endObject();
            //获取响应
            IndexResponse  response=this.client.prepareIndex("books","novel").setSource(content).get();
            return  new ResponseEntity(response.getId(),HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("delete/books/novel")
    @ResponseBody
    public ResponseEntity delete(@RequestParam(value = "id") String id){
        if(id.isEmpty()){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        DeleteResponse response=this.client.prepareDelete("books","novel",id).get();
        return new ResponseEntity(response.getId(),HttpStatus.OK);
    }
    @PutMapping("updata/books/novel")
    @ResponseBody
    public ResponseEntity updata(
            @RequestParam(value = "id") String id,
            @RequestParam(value = "title",required = false) String title,
            @RequestParam(value = "author",required = false) String author
    ){
        UpdateRequest request=new UpdateRequest("books","novel",id);
        try {
            XContentBuilder builder=XContentFactory.jsonBuilder()
                    .startObject();
            if(title!=null){
                builder.field("title",title);
            }
            if(author!=null){
                builder.field("author",author);
            }
            builder.endObject();
            request.doc(builder);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
           UpdateResponse response= this.client.update(request).get();
            return new ResponseEntity(response.getResult().toString(),HttpStatus.OK);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    @PostMapping("query/books/novel")
    @ResponseBody
    public  ResponseEntity query(
            @RequestParam(name="title" ,required = false)String title,
            @RequestParam(name="author" ,required = false)String author,
            @RequestParam(name="gt_word_count",defaultValue = "0")int gt_word_count,
            @RequestParam(name="lt_word_count",required = false) Integer lt_word_count
    ){
        BoolQueryBuilder queryBuilder= QueryBuilders.boolQuery();
        if(author!=null){
            queryBuilder.must(QueryBuilders.matchQuery("author",author));
        }
        if(title!=null){
            queryBuilder.must(QueryBuilders.matchQuery("title",title));
        }
        //大于
        RangeQueryBuilder rangeQueryBuilder=QueryBuilders.rangeQuery("word_count").from(gt_word_count);
        //小于
        if(lt_word_count!=null&&lt_word_count>0){
            rangeQueryBuilder.to(lt_word_count);
        }
        //过滤条件
        queryBuilder.filter(rangeQueryBuilder);
          SearchRequestBuilder builder= client.prepareSearch("books")
                .setTypes("novel")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(queryBuilder)
                .setFrom(0)
                .setSize(10);
        System.out.println(builder);
        SearchResponse response =builder.get();
        List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
        //将命中的数据放入List
        for(SearchHit hit:response.getHits()){
            result.add(hit.getSource());
        }
        return new ResponseEntity(result,HttpStatus.OK);
    }
}
