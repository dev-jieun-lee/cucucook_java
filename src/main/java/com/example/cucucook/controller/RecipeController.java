package com.example.cucucook.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.MemberRecipe;
import com.example.cucucook.domain.PublicRecipe;
import com.example.cucucook.domain.RecipeCategory;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;
import com.example.cucucook.service.RecipeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/recipe")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    // 공공레시피 목록조회
    @GetMapping(value = "/getPublicRecipeList")
    public ApiResponse<List<PublicRecipe>> getPublicRecipeList(
            @RequestParam(value = "search", defaultValue = "") String search,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "display", defaultValue = "20") int display,
            @RequestParam(value = "recipeCategoryId", defaultValue = "") String recipeCategoryId) {

        return recipeService.getPublicRecipeList(search, start, display, recipeCategoryId);
    }

    // 공공레시피 상세조회
    @GetMapping(value = "/getPublicRecipe")
    public ApiResponse<PublicRecipe> getPublicRecipe(@RequestParam String search,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "display", defaultValue = "20") int display) {
        return recipeService.getPublicRecipe(search, start, display);
    }

    // 회원 레시피 목록조회
    @GetMapping(value = "/getMemberRecipeList")
    public ApiResponse<List<MemberRecipe>> getMemberRecipeList(@RequestParam String search,
            @RequestParam String recipeCategoryId,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "display", defaultValue = "20") int display,
            @RequestParam String orderby) {

        return recipeService.getMemberRecipeList(search, recipeCategoryId, start,
                display, orderby);
    }

    // 회원레시피 조회
    @GetMapping(value = "/getMemberRecipe")
    public ApiResponse<HashMap<String, Object>> getMemberRecipe(@RequestParam String recipeId,
            @RequestParam(value = "isUpdate", defaultValue = "false") boolean isUpdate) {
        return recipeService.getMemberRecipe(recipeId, isUpdate);
    }

    // 회원레시피등록
    @PostMapping(value = "/insertMemberRecipe", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ApiResponse<Integer> insertMemberRecipe(
            @RequestPart("recipeInfo") String recipeInfoJson,
            @RequestPart("recipeIngredients") String recipeIngredientsJson,
            @RequestPart("thumbnail") MultipartFile thumbnail,
            @RequestPart("recipeProcessContents") String recipeProcessContentsJson,
            @RequestPart("recipeProcessImages") List<MultipartFile> recipeProcessImages
    //
    ) throws JsonProcessingException, Exception {
        // map으로 만들어서 서비스로 넘기기 (파일은 각각 넘겨줘야함)
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> recipeInfo = objectMapper.readValue(
                recipeInfoJson, new TypeReference<Map<String, Object>>() {
                });
        List<Map<String, String>> recipeIngredients = objectMapper.readValue(
                recipeIngredientsJson, new TypeReference<List<Map<String, String>>>() {
                });
        List<String> recipeProcessContents = objectMapper.readValue(
                recipeProcessContentsJson, new TypeReference<List<String>>() {
                });

        Map<String, Object> memberRecipeInfo = new HashMap<>();
        memberRecipeInfo.put("recipeInfo", recipeInfo);
        memberRecipeInfo.put("recipeIngredients", recipeIngredients);
        memberRecipeInfo.put("recipeProcessContents", recipeProcessContents);

        return recipeService.insertMemberRecipe(memberRecipeInfo, thumbnail, recipeProcessImages);
    }

    // 회원레시피수정
    @PostMapping(value = "/updateMemberRecipe", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ApiResponse<Integer> updateMemberRecipe(
            @RequestParam("recipeId") String recipeId,
            @RequestPart("recipeInfo") String recipeInfoJson,
            @RequestPart("recipeIngredients") String recipeIngredientsJson,
            @RequestPart("thumbnail") MultipartFile thumbnail,
            @RequestPart("recipeProcessContents") String recipeProcessContentsJson,
            @RequestPart("recipeProcessImages") List<MultipartFile> recipeProcessImages,
            @RequestParam("thumbnailServerImgId") String thumbnailServerImgId)
            throws JsonProcessingException, Exception {
        // map으로 만들어서 서비스로 넘기기 (파일은 각각 넘겨줘야함)
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> recipeInfo = objectMapper.readValue(
                recipeInfoJson, new TypeReference<Map<String, Object>>() {
                });
        List<Map<String, String>> recipeIngredients = objectMapper.readValue(
                recipeIngredientsJson, new TypeReference<List<Map<String, String>>>() {
                });
        List<String> recipeProcessContents = objectMapper.readValue(
                recipeProcessContentsJson, new TypeReference<List<String>>() {
                });

        Map<String, Object> memberRecipeInfo = new HashMap<>();
        memberRecipeInfo.put("recipeInfo", recipeInfo);
        memberRecipeInfo.put("recipeIngredients", recipeIngredients);
        memberRecipeInfo.put("recipeProcessContents", recipeProcessContents);

        return recipeService.updateMemberRecipe(memberRecipeInfo, thumbnail,
                recipeProcessImages, thumbnailServerImgId);
    }

    // 회원 레시피 삭제
    @DeleteMapping(value = "/deleteMemberRecipe")
    public ApiResponse<Integer> updateMemberRecipe(@RequestParam String recipeId) throws Exception {
        return recipeService.deleteMemberRecipe(recipeId);
    }

    // 레시피 카테고리 목록(카운트별 레시피 카운트수 포함)
    @GetMapping(value = "/getRecipeCategoryListWithMemberRecipeCount")
    public ApiResponse<List<RecipeCategory>> getRecipeCategoryListWithMemberRecipeCount(@RequestParam String search) {
        return recipeService.getRecipeCategoryListWithMemberRecipeCount(search);
    }

    // 레시피 카테고리 목록(레시피 작성을 위함)
    @GetMapping(value = "/getRecipeCategoryListForWrite")
    public ApiResponse<HashMap<String, Object>> getRecipeCategoryListForWrite() {

        return recipeService.getRecipeCategoryListForWrite();
    }

    // 회원 레시피 별 댓글 조회
    @GetMapping(value = "/getRecipeCommentList")
    public ApiResponse<List<RecipeComment>> getRecipeCommentList(@RequestParam String recipeId,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "display", defaultValue = "20") int display) {
        return recipeService.getRecipeCommentList(recipeId, start, display);
    }

    // 회원 레시피 댓글 상세조회
    @GetMapping(value = "/getRecipeComment")
    public ApiResponse<RecipeComment> getRecipeComment(@RequestParam String recipeId, @RequestParam String commentId) {
        return recipeService.getRecipeComment(recipeId, commentId);
    }

    // 회원 레시피 댓글 등록
    @PostMapping(value = "/insertRecipeComment")
    public ApiResponse<Integer> insertRecipeComment(@RequestBody RecipeComment recipeComment) throws Exception {
        return recipeService.insertRecipeComment(recipeComment);
    }

    // 회원 레시피 댓글 수정
    @PutMapping(value = "/updateRecipeComment")
    public ApiResponse<Integer> updateRecipeComment(@RequestBody RecipeComment recipeComment) throws Exception {
        return recipeService.updateRecipeComment(recipeComment);
    }

    // 회원 레시피 댓글 삭제
    @DeleteMapping(value = "/deleteRecipeComment")
    public ApiResponse<Integer> deleteRecipeComment(@RequestParam String recipeId, @RequestParam String commentId,
            @RequestParam boolean hasChildComment) throws Exception {
        return recipeService.deleteRecipeComment(recipeId, commentId);
    }

    // 회원레시피 댓글 삭제(본댓글에 대댓글이 남겨져 있는 경우)
    @PutMapping(value = "/deleteRecipeCommentHasChild")
    public ApiResponse<Integer> deleteRecipeCommentHasChild(@RequestParam String recipeId,
            @RequestParam String commentId,
            @RequestParam boolean hasChildComment) throws Exception {
        return recipeService.deleteRecipeCommentHasChild(recipeId, commentId);
    }

    // 레시피 카테고리 목록 가져오기
    @GetMapping(value = "/getRecipeCategoryList")
    public ApiResponse<List<RecipeCategory>> getRecipeCategoryList(@RequestParam String search,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "display", defaultValue = "10") int display) {
        return recipeService.getRecipeCategoryList(search, start, display);
    }

    // 회원 레시피 좋아요 등록 (회원직접등록)
    @PostMapping(value = "/insertMemberRecipeLike")
    public ApiResponse<Integer> insertMemberRecipeLike(@RequestBody RecipeLike recipeLike) throws Exception {
        return recipeService.insertMemberRecipeLike(recipeLike);
    }

    // 회원 레시피 좋아요 삭제 (회원직접삭제)
    @DeleteMapping(value = "/deleteMemberRecipeLike")
    public ApiResponse<Integer> deleteMemberRecipeLike(@RequestParam String recipeId, @RequestParam int memberId)
            throws Exception {
        return recipeService.deleteMemberRecipeLike(recipeId, memberId);
    }

}
