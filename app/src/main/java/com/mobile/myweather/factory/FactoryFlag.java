/*
 * Copyright 2015 Jorge Manrique
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mobile.myweather.factory;


import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.mobile.myweather.app.R;

public class FactoryFlag {

    public static Integer getFlag(String country,Resources res,Context context) {

        try {
            country=country.toLowerCase();
            if ("gb".equals(country.toLowerCase())){
                country="uk";
            }
            else if ("italy".equals(country)){
                country="it";
            }
            else if ("spain".equals(country)){
                country="es";
            }
            else if ("united states of america".equals(country)){
                country="us";
            }
            else if ("morocco".equals(country)){
                country="ma";
            }
            else if ("poland".equals(country)){
                country="pl";
            }
            else if ("france".equals(country)){
                country="fr";
            }
            else if ("germany".equals(country)){
                country="de";
            }
            int result = res.getIdentifier(country, "drawable", context.getPackageName());
            if (result==0){
                return R.drawable.eu;
            }
            return result;

        } catch (Exception ex) {
            return R.drawable.eu;
        }
    }
}
