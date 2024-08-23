package com.example.cucucook.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.MemberRecipe;
import com.example.cucucook.service.RecipeService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/recipe")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @GetMapping(value = "/getMemberRecipeList")
    public ApiResponse<List<MemberRecipe>> getMemberRecipeList(@RequestParam String search, @RequestParam String recipeCategoryId, @RequestParam(value = "start", required = false, defaultValue = "1") int start, @RequestParam(value = "display", required = true, defaultValue = "20") int display, @RequestParam String orderby, @RequestParam String division) {
        return recipeService.getMemberRecipeList(search, recipeCategoryId, start, display, orderby, division);
    }

    @GetMapping(value = "/getMemberRecipe")
    public ApiResponse<HashMap<String, Object>> getMemberRecipe(@RequestParam String recipeId) {
        return recipeService.getMemberRecipe(recipeId);
    }

    @PostMapping(value = "/insertMemberRecipe")
    public ApiResponse<Integer> insertMemberRecipe(@RequestBody MemberRecipe memberRecipe) {
        return recipeService.insertMemberRecipe(memberRecipe);
    }

    @PatchMapping(value = "/updateMemberRecipe")
    public ApiResponse<Integer> updateMemberRecipe(@RequestBody MemberRecipe memberRecipe) {
        return recipeService.updateMemberRecipe(memberRecipe);
    }

    @DeleteMapping(value = "/deleteMemberRecipe")
    public ApiResponse<Integer> updateMemberRecipe(@RequestParam String recipeId) {
        return recipeService.deleteMemberRecipe(recipeId);
    }

}
