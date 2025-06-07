/*
 * Copyright (C) 2025 by ESI Tech Vietnam and associated contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esql.data;

import java.text.ParsePosition;
import java.util.Arrays;

/**
 *
 * 
 * This class provides a state machine to check a string value against a pattern
 * with left and right sequences, valid characters, and optional sequences.
 * It supports checking for patterns like hex strings, integer strings, etc.
 * The state machine has the following states:
 * - STRING_START: start state
 * - STRING_LEFT_SEQUENCE_MATCH: after matching start sequence
 * - STRING_CONTINUE_LEFT_SEQUENCE: continue matching start sequence
 * - STRING_START_OF_VALUE: start of value after left sequence
 * - STRING_CONTINUE_OF_VALUE: continue matching value characters
 * - STRING_RIGHT_SEQUENCE_MATCH: after matching right sequence
 * - STRING_CONTINUE_RIGHT_SEQUENCE: continue matching right sequence
 * - STRING_END_OF_TEMPLATE: end of template after right sequence
 * - STRING_GRACEFULLY_END_MATCHED: end of value matched gracefully
 * 
 * 
 @startuml
title String Left/Right Sequence Checker State Machine

state LeftSequenceChoice <<choice>>
[*] --> StartString


StartString --> StartString: bypass Surrounding Spaces
StartString --> LeftSequenceChoice
LeftSequenceChoice -> LeftSequenceMatch: match first char
LeftSequenceMatch --> ContinueSequenceMatch
ContinueSequenceMatch --> StartOfValue: sequence match complete
ContinueSequenceMatch -> ContinueSequenceMatch: next char
LeftSequenceChoice ---> StartOfValue: unmatch

StartOfValue --> ContinueOfValue
ContinueOfValue -> ContinueOfValue: char in set

state RightSequenceChoice <<choice>>
ContinueOfValue --> RightSequenceChoice
RightSequenceChoice -> RightSequenceMatch
RightSequenceMatch --> ContinueRightSequence
ContinueRightSequence -> ContinueRightSequence
ContinueRightSequence --> EndOfTemplate
RightSequenceChoice ---> EndOfTemplate: no right
EndOfTemplate -> EndOfTemplate: bypass Surrounding Spaces
EndOfTemplate --> [*]

@enduml

*
*/

public class StringLeftRightChecker {

    public static final int STRING_START = 0;
    public static final int STRING_START_SEQUENCE_MATCH = 1;
    public static final int STRING_CONTINUE_START_SEQUENCE = 2;
    public static final int STRING_START_OF_VALUE = 3;
    public static final int STRING_CONTINUE_OF_VALUE = 4;
    public static final int STRING_RIGHT_SEQUENCE_MATCH = 5;
    public static final int STRING_CONTINUE_RIGHT_SEQUENCE = 6;
    public static final int STRING_END_OF_TEMPLATE = 7;
    public static final int STRING_GRACEFULLY_END_MATCHED = 8; //end of value matched, can be used to check if matched

    /**
     * Check a string value of pattern, like a string with start/end chars and valid chars.
     * This is used for string validation, extract meaningfully chars as-well
     * it support checking like a hex string, integer string, etc.
     *
     * @param bypassSurroundingSpaces if true, will skip leading/trailing spaces and tabs
     * @param includeLeftSequence    if true, will include start sequence in outputStart
     * @param leftSequence  the start char sequence, can be null or empty
     * @param leftIsOptional         if true, the start char sequence is optional, if not found, will go to value directly
     * @param leftAsTheSet           if true, the start char sequence is treated as a set of characters, otherwise as a sequence
     * @param rightCharSequence         the end char sequence, can be null or empty
     * @param rightIsOptional           if true, the end char sequence is optional, if not found, will go to end directly
     * @param rightAsTheSet             if true, the end char sequence is treated as a set of characters, otherwise as a sequence
     * @param validSortedChars        the valid sorted chars, must be sorted in ascending order, can be null or empty
     * @param allowLineBreaks         if true, will allow line breaks in the value, otherwise will stop at line breaks
     * @param stringValue             the string value to check, must not be null or empty
     * @param outputStart             position to start match, can be null, if not null, will set the index to the start of value
     * @param outputEnd               position to end match, can be null, if not null, will set the index to the end of value
     * @return last state of the check, if it is 6, means the string value is matched with the pattern.
     */
    public static int checkStringOfPattern(
            int initialState,
            boolean bypassSurroundingSpaces,
            boolean includeLeftSequence, char[] leftSequence, boolean leftIsOptional, boolean leftAsTheSet,
            boolean includeRightSequence, char[] rightCharSequence, boolean rightIsOptional, boolean rightAsTheSet,
            char[] validSortedChars, boolean allowLineBreaks,
            CharSequence stringValue, ParsePosition outputStart, ParsePosition outputEnd) {
        if (stringValue == null || stringValue.length() == 0
                || validSortedChars == null || validSortedChars.length == 0)
            return -1; //not match anyway or empty string

        int state = initialState;
        if (state < STRING_START || state >= STRING_GRACEFULLY_END_MATCHED) {
            state = STRING_START; //reset to start state
        }

        int patternPos = 0;
        int initialIndex = 0; //initial index of stringValue
        if (outputStart != null) {
            initialIndex = outputStart.getIndex();
            if (initialIndex < 0)
                initialIndex = 0; //reset to 0
        }

        for (int i = initialIndex; i <= stringValue.length(); i++) {
            char c = i < stringValue.length() ? stringValue.charAt(i) : ' ';
            if (state == STRING_START && bypassSurroundingSpaces && Character.isWhitespace(c)) {
                continue; //skip leading spaces
            }
            if (state == STRING_START) {
                state = STRING_START_OF_VALUE;

                //choice to include left sequence or not
                if (leftSequence != null && leftSequence.length > 0) {
                    if(leftAsTheSet) { //start as a set of characters
                        boolean valid = Arrays.binarySearch(leftSequence, c) >= 0; //check only first char
                        if (!valid && !leftIsOptional) {
                            return state; //must have start char match
                        }

                        state = STRING_START_OF_VALUE;
                        if (includeLeftSequence && outputStart != null) {
                            outputStart.setIndex(i); //set start index
                        }

                        if(valid) {
                            continue; //jump to next char as Start Of Value
                        }
                    }
                    else {
                        // matching the start character sequence
                        boolean valid = c == leftSequence[0]; //check first char
                        if (!valid && !leftIsOptional) {
                            return state; //must have start char match
                        }
                        if (includeLeftSequence && outputStart != null) {
                            outputStart.setIndex(i); //set start index
                        }
                        if (valid) {
                            state = STRING_START_SEQUENCE_MATCH; //checked start chars
                        }
                        else
                            state = STRING_START_OF_VALUE; //next to value directly
                    }
                }
            }

            if (state == STRING_START_SEQUENCE_MATCH || state == STRING_CONTINUE_START_SEQUENCE) { //after start chars
                if (patternPos == leftSequence.length) { //found all start chars
                    state = STRING_START_OF_VALUE; //go to value
                    if (!includeLeftSequence && outputStart != null)
                        outputStart.setIndex(i); //set start index
                } else if (patternPos < leftSequence.length && c == leftSequence[patternPos]) {
                    state = STRING_CONTINUE_START_SEQUENCE;
                    patternPos++; //next char matched
                    continue;
                } else if (leftIsOptional) { //optional start chars, go to value directly
                    state = STRING_START_OF_VALUE;
                    i -= patternPos; //reset back to the char at beginning of value
                    if (outputStart != null)
                        outputStart.setIndex(i); //set start index
                } else {
                    return state; //not match start chars
                }
            }

            if (state == STRING_START_OF_VALUE || state == STRING_CONTINUE_OF_VALUE) { //in value or after line break
                if (allowLineBreaks && state == STRING_CONTINUE_OF_VALUE && (c == '\n' || c == '\r')) { //line break in value allowed
                    continue;
                }
                if(state == STRING_START_OF_VALUE) {
                    if (!includeLeftSequence && outputStart != null) {
                        outputStart.setIndex(i); //set start index
                    }
                }

                //Check for the valid char
                if (Arrays.binarySearch(validSortedChars, c) < 0 && i < stringValue.length()) { //not valid chars or reached end of string
                    state = STRING_RIGHT_SEQUENCE_MATCH; //invalid char in value, it should be the end of value
                    if (!includeRightSequence && outputEnd != null)
                        outputEnd.setIndex(i); //set end index to current char
                } else if(i < stringValue.length()) { //valid char in value
                    state = STRING_CONTINUE_OF_VALUE; //continue in value
                    continue;
                } else {
                    if (!includeRightSequence && outputEnd != null)
                        outputEnd.setIndex(i); //set end index to current char
                    if(state == STRING_CONTINUE_OF_VALUE) //at least one char
                        state = rightIsOptional ? STRING_END_OF_TEMPLATE : STRING_RIGHT_SEQUENCE_MATCH;
                }
            }

            if (state == STRING_RIGHT_SEQUENCE_MATCH) { //after value, check end chars
                if (rightCharSequence != null && rightCharSequence.length > 0) {
                    if (rightAsTheSet) { //end as a set of characters
                        boolean valid = Arrays.binarySearch(rightCharSequence, c) >= 0; //check for series of characters
                        if (!valid && !rightIsOptional) {
                            return state; //must have end char match
                        }
                        state = STRING_CONTINUE_RIGHT_SEQUENCE; //go to end directly
                    } else {
                        if (c == rightCharSequence[0]) {
                            state = STRING_CONTINUE_RIGHT_SEQUENCE; //checked end chars
                        } else if (!rightIsOptional) //not optional end chars, invalid char in value
                            return state;

                        if (state != STRING_CONTINUE_RIGHT_SEQUENCE) {
                            if (i + 1 < stringValue.length()) { //no end char sequence, must be at end of value
                                return state; //not at end of value
                            } else
                                state = STRING_END_OF_TEMPLATE; //go to end directly
                        }
                    }
                }
                else {//no right sequence
                    state = STRING_END_OF_TEMPLATE;
                }
            }

            if (state == STRING_CONTINUE_RIGHT_SEQUENCE) { //in sequence of end chars
                if (rightAsTheSet) { //end as a set of characters
                    if(i >= stringValue.length())
                        state = STRING_GRACEFULLY_END_MATCHED;
                    else {
                        boolean valid = Arrays.binarySearch(rightCharSequence, c) >= 0; //check for series of characters
                        if (!valid) {
                            if(rightIsOptional)
                                state = STRING_END_OF_TEMPLATE;
                            else
                                return state;
                        }
                        else
                            continue;
                    }
                } else {
                    if (patternPos == rightCharSequence.length) { //found all end chars
                        state = i + 1 >= stringValue.length() ? STRING_GRACEFULLY_END_MATCHED : STRING_END_OF_TEMPLATE; //go to end
                        if (includeRightSequence && outputEnd != null)
                            outputEnd.setIndex(i);
                    } else if (patternPos < rightCharSequence.length && c == rightCharSequence[patternPos]) {
                        patternPos++;
                        continue; //continue to next char
                    } else if (rightIsOptional) { //optional end chars, go to end directly
                        state = STRING_END_OF_TEMPLATE;
                        if (includeRightSequence)
                            return state; //can not pass the end chars, so not match
                        i -= patternPos; //reset back to the char at beginning of value
                        if (outputEnd != null)
                            outputEnd.setIndex(i);
                    } else {
                        return state; //neither match end chars, nor optional
                    }
                }
            }

            if (state == STRING_END_OF_TEMPLATE) { //after all chars, must be the last char in stringValue
                if(i >= stringValue.length()) { //beyond the end
                    if(rightIsOptional || rightCharSequence == null || rightCharSequence.length == 0)
                        state = STRING_GRACEFULLY_END_MATCHED;
                    else
                        return state;
                }

                if(i < stringValue.length() && (!Character.isWhitespace(c) || bypassSurroundingSpaces))
                    return state;
            }
        }

        //if (state == STRING_END_OF_TEMPLATE) { //end of value gracefully
        //    state = STRING_GRACEFULLY_END_MATCHED;
        //}

        return state; //return last state
    }
}
