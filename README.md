# String Utilities Mod
A mod that provides more NBT string operations.\
一个提供更多 NBT 字符串操作的模组。

## Syntax 语法
`/string <method> [target] [source...]`

The `target` and `source` parts are similar to what follows the `data modify` command.\
`target`和`source`部分与`data modify`命令后面的部分类似。

## Example 例子
`/string isEmpty value ""` returns 1\
`/string indexOf value "asdfghjkl" value "qwerty"` returns -1\
`/string escape storage a:b c value '\'quote"slash\\'` stores `'quote\"slash\\` into `foo:bar/baz`\
`/string strip storage foo:bar baz value "  a 123   "` stores `a 123` into `foo:bar/baz`\
`/string concat2 storage a b from entity @s SelectedItem.id from entity @s Inventory[0].id`\
`/string join storage test playerUUID from value "~" from @s UUID`\

## Available Methods 可用的方法
| Name               | Source                                          | Target                               | Return Value                                                                                 |
|--------------------|-------------------------------------------------|--------------------------------------|----------------------------------------------------------------------------------------------|
| isBlank            | String                                          | -                                    | 1 if the input is a blank string, 0 otherwise.                                               |
| isEmpty            | String                                          | -                                    | 1 if the input is a empty string, 0 otherwise.                                               |
| length             | String                                          | -                                    | String length.                                                                               |
| toString           | Any NBT Element                                 | String                               | 1                                                                                            |
| escape             | String                                          | String                               | 1                                                                                            |
| escapeNbt          | String                                          | String                               | 1                                                                                            |
| escapeRegex        | String                                          | String                               | 1                                                                                            |
| toLowerCase        | String                                          | String                               | 1                                                                                            |
| toUpperCase        | String                                          | String                               | 1                                                                                            |
| strip              | String                                          | String                               | 1                                                                                            |
| stripLeading       | String                                          | String                               | 1                                                                                            |
| stripTrailing      | String                                          | String                               | 1                                                                                            |
| toCharArray        | String                                          | List\<String\>                       | 1                                                                                            |
| toCodePointStrings | String                                          | List\<String\>                       | Count of code points.                                                                        |
| toCodePoints       | String                                          | IntArray                             | Count of code points.                                                                        |
| fromCodePoints     | IntArray                                        | String                               | 1                                                                                            |
| concat             | List\<String\>                                  | String                               | 1                                                                                            |
| concat2            | String s0, String s1, \[String s2\]             | String                               | 1                                                                                            |
| trim               | String src, \[String trimChars\]                | String                               | 1                                                                                            |
| trimStart          | String src, \[String trimChars\]                | String                               | 1                                                                                            |
| trimEnd            | String src, \[String trimChars\]                | String                               | 1                                                                                            |
| at                 | String src, Int index                           | String                               | 1                                                                                            |
| repeat             | String src, Int count                           | String                               | 1                                                                                            |
| matchesAll         | String src, String pattern                      | List\<Compound(Int start, Int end)\> | Count of match results.                                                                      |
| matchesAllFully    | String src, String pattern                      | List\<Compound(Int start, Int end)\> | Count of match results.                                                                      |
| join               | String delimiter, List elements                 | String                               | 1                                                                                            |
| substring          | String src, Int begin, \[Int end\]              | String                               | 1                                                                                            |
| substring2         | String src, Int begin, \[Int length\]           | String                               | 1                                                                                            |
| split              | String src, String separator, \[Int limit\]     | List\<String\>                       | Count of splitted string parts                                                               |
| indexOf            | String src, String substring, \[Int fromIndex\] | String                               | The index of the first occurrence of the substring, or -1 if the substring is not contained. |
| lastIndexOf        | String src, String substring, \[Int fromIndex\] | String                               | The index of the last occurrence of the substring, or -1 if the substring is not contained.  |
| startsWith         | String src, String prefix, \[Int offset\]       | -                                    | 1 if the second input is a prefix of the first input, 0 otherwise.                           |
| endsWith           | String src, String suffix                       | -                                    | 1 if the second input is a suffix of the first input, 0 otherwise.                           |
| contains           | String src, String substring                    | -                                    | 1 if the first input contains the second input, 0 otherwise.                                 |
| matches            | String src, String regex                        | -                                    | 1 if the first input matches the second input, 0 otherwise.                                  |
| replace            | String src, String target, String replacement   | String                               | 1                                                                                            |
| replaceAll         | String src, String regex, String replacement    | String                               | 1                                                                                            |
| replaceFirst       | String src, String regex, String replacement    | String                               | 1                                                                                            |

Note: Some methods actually accept any NBT element, but convert it to a string before processing.\
注：有些方法实际上接受任何 NBT 元素，但在处理之前将其转换为字符串。