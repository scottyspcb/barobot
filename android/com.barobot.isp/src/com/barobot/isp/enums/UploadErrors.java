/*
 * Copyright (C) 2013 Keisuke SUZUKI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * Distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This code has built in knowledge of avrdude.
 * Thanks to avrdude coders
 *  Brian S. Dean, Joerg Wunsch, Eric Weddington, Jan-Hinnerk Reichert,
 *  Alex Shepherd, Martin Thomas, Theodore A. Roth, Michael Holzt
 *  Colin O'Flynn, Thomas Fischl, David Hoerl, Michal Ludvig,
 *  Darell Tan, Wolfgang Moser, Ville Voipio, Hannes Weisbach,
 *  Doug Springer, Brett Hagman
 *  and all contributers.
 */

package com.barobot.isp.enums;

public enum UploadErrors {
    AVR_CHIPTYPE    (1,    "Unexpected AVR chip type."),
    FILE_OPEN       (2,    "Cannot open file."),
    HEX_FILE_OPEN   (3,    "Illegal .hex file."),

    HEX_STREAM_NUll (25,   "HEX stream null"),
    
    HEX_FILE_OPEN1  (30,   "FileNotFoundException."),
    HEX_FILE_OPEN2  (31,   "HEX_FILE_OPEN2."),
    HEX_FILE_OPEN3  (32,   "HEX_FILE_OPEN3."),

    CHIP_INIT       (4,    "Cannot initialize a chip."),
    SIGNATURE       (5,    "Incorrect chip type."),
    PAGE_WRITE      (6,    "An unexpected error occurred while writing"),
    OPEN_DEVICE     (7,    "Cannot open device.");

    private final int code;
    private final String description;

    private UploadErrors(int code, String description) {
      this.code = code;
      this.description = description;
    }
    public String getDescription() {
       return description;
    }
    public int getCode() {
       return code;
    }
    @Override
    public String toString() {
      return description;
    }
}
