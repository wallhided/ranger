/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ranger.plugin.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ranger.plugin.policyengine.RangerAccessRequest;
import org.apache.ranger.plugin.policyengine.RangerRequestScriptEvaluator;

import javax.script.ScriptEngine;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RangerRequestExprResolver {
    private static final Log LOG = LogFactory.getLog(RangerRequestExprResolver.class);

    private static final String  REGEX_GROUP_EXPR   = "expr";
    private static final String  SCRIPT_ENGINE_NAME = "JavaScript";
    private static final Pattern PATTERN            = Pattern.compile("\\$\\{\\{(?<" + REGEX_GROUP_EXPR + ">.*?)\\}\\}");
    public  static final String  EXPRESSION_START   = "${{";

    private final String  str;
    private final String  serviceType;
    private final boolean hasTokens;


    public RangerRequestExprResolver(String str, String serviceType) {
        this.str         = str;
        this.serviceType = serviceType;
        this.hasTokens   = hasExpressions(str);

        if (LOG.isDebugEnabled()) {
            LOG.debug("RangerRequestExprResolver(" + str + "): hasTokens=" + hasTokens);
        }
    }

    public String resolveExpressions(RangerAccessRequest request) {
        String ret = str;

        if (hasTokens) {
            RangerRequestScriptEvaluator scriptEvaluator = new RangerRequestScriptEvaluator(request);
            ScriptEngine                 scriptEngine    = ScriptEngineUtil.createScriptEngine(SCRIPT_ENGINE_NAME, serviceType);
            StringBuffer                 sb              = new StringBuffer();
            Matcher                      matcher         = PATTERN.matcher(str);

            while (matcher.find()) {
                String expr = matcher.group(REGEX_GROUP_EXPR);
                String val  = Objects.toString(scriptEvaluator.evaluateScript(scriptEngine, expr));

                matcher.appendReplacement(sb, val);
            }

            matcher.appendTail(sb);

            ret = sb.toString();

            if (LOG.isDebugEnabled()) {
                LOG.debug("RangerRequestExprResolver.processExpressions(" + str + "): ret=" + ret);
            }
        }

        return ret;
    }

    public static boolean hasExpressions(String str) {
        Matcher matcher = PATTERN.matcher(str);

        return matcher.find();
    }
}
