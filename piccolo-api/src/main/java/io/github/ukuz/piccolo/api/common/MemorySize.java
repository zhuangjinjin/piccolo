/*
 * Copyright 2019 ukuz90
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ukuz.piccolo.api.common;

/**
 * @author ukuz90
 */
public final class MemorySize {

    private MemorySize() {}

    public static String prettyMemorySize(long size) {
        MemorySizeUnit[] unit = MemorySizeUnit.values();
        int i = 0;
        double result = 0;
        for (; i < unit.length; i++) {
            long base = unit[i].getBaseVal();
            if (size / base < 1024) {
                result = (double)size / (double)base;
                break;
            }
        }
        return String.format("%.2f%s", result, unit[i].getUnitDesc());
    }

    public static String prettyMemorySize(long size, MemorySizeUnit unit) {
        double result = (double)size / (double)unit.getBaseVal();
        return String.format("%.2f%s", result, unit.getUnitDesc());
    }

    public enum MemorySizeUnit {
        B("B", 1),
        KB("KB", 1024L),
        MB("MB", 1024L * 1024L),
        GB("GB", 1024L * 1024L * 1024L),
        TB("TB", 1024L * 1024L * 1024L * 1024L),
        PB("PB", 1024L * 1024L * 1024L * 1024L * 1024L),
        EB("EB", 1024L * 1024L * 1024L * 1024L * 1024L * 1024L),
        ;
        private final String unitDesc;
        private final long baseVal;

        MemorySizeUnit(String unitDesc, long baseVal) {
            this.unitDesc = unitDesc;
            this.baseVal = baseVal;
        }

        public String getUnitDesc() {
            return unitDesc;
        }

        public long getBaseVal() {
            return baseVal;
        }
    }

    public static void main(String[] args) {
        System.out.println(MemorySize.prettyMemorySize(Long.MAX_VALUE));
    }

}
