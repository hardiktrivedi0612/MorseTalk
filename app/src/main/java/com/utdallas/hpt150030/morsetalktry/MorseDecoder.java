package com.utdallas.hpt150030.morsetalktry;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by roshan on 27/03/16.
 */
public class MorseDecoder
{
    private HashMap<String, String> hashMap = new HashMap<String, String>();
    public MorseDecoder()
    {
        hashMap.put("du","A"); hashMap.put("uddd","B"); hashMap.put("udud","C"); hashMap.put("udd","D"); hashMap.put("d","E");
        hashMap.put("dud","F"); hashMap.put("uud","G"); hashMap.put("dddd","H"); hashMap.put("dd","I"); hashMap.put("duuu","J");
        hashMap.put("udu","K"); hashMap.put("dudd","L"); hashMap.put("uu","M"); hashMap.put("ud","N"); hashMap.put("uuu","O");
        hashMap.put("duud","P"); hashMap.put("uudu","Q"); hashMap.put("dud","R"); hashMap.put("ddd","S"); hashMap.put("u","T");
        hashMap.put("ddu","U"); hashMap.put("dddu","V"); hashMap.put("duu","W"); hashMap.put("uddu","X"); hashMap.put("uduu","Y");
        hashMap.put("uudd","Z"); hashMap.put("duuuu","1"); hashMap.put("dduuu","2"); hashMap.put("ddduu","3"); hashMap.put("ddddu","4");
        hashMap.put("ddddd","5"); hashMap.put("udddd","6"); hashMap.put("uuddd","7"); hashMap.put("uuudd","8"); hashMap.put("uuuud","9");
        hashMap.put("uuuuu","0");
    }


    public String decode(String inpString)
    {
        Log.d("MorseDecoder",inpString);
        StringBuilder output = new StringBuilder();
        String[] words = inpString.split("w");
        for(String word : words)
        {
            String[] letters = word.split("c");
            for(String letter : letters)
            {
                if(hashMap.containsKey(letter)) {
                    output.append(hashMap.get(letter));
                }else{
                    return null;
                }
            }

            output.append(" ");
        }
        Log.d("MorseDecoder",output.toString());
        return output.toString();
    }
}
