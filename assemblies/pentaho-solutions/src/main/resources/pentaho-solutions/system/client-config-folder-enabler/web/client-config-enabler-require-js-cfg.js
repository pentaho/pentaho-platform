/*!
 * Copyright 2017 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function(global) {
  /* globals requireCfg, CONTEXT_PATH */
  /* eslint dot-notation: 0, require-jsdoc: 0 */

  var requireTypes = requireCfg.config["pentaho/service"] || (requireCfg.config["pentaho/service"] = {});

  requireCfg.paths["pentaho/config/deploy"] = CONTEXT_PATH + "content/config/deploy";
  requireTypes["pentaho/config/deploy/config"] = "pentaho.config.spec.IRuleSet";

})(this);
