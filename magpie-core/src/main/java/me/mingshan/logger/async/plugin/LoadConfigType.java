/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.mingshan.logger.async.plugin;

/**
 * Supports type of getting configuration.
 *
 * @author mingshan
 */
public enum LoadConfigType {

    /**
     * Load configuration from system property.
     */
    SYSTEM("Load configuration from system property"),

    /**
     * Load configuration from system property.
     */
    FILE("Load configuration from file");

    /**
     * The description of loading configuration.
     */
    private String desc;

    LoadConfigType(String desc) {
        this.desc = desc;
    }

    private String getDesc() {
        return this.desc;
    }

}
