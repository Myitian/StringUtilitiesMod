# String Utilities Mod

<img src="common/src/main/resources/logo.png" alt="logo" width="128"/>

[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1156276?style=for-the-badge&logo=curseforge&label=CurseForge%20Downloads&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/string_utilities)\
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/VE8pf7zL?style=for-the-badge&logo=modrinth&label=Modrinth%20Downloads&color=00AF5C)](https://modrinth.com/mod/string_utilities)\
[![MC百科](https://img.shields.io/badge/mcmod.cn-MC%E7%99%BE%E7%A7%91-58b6d8?style=for-the-badge)](https://www.mcmod.cn/class/13733.html)

A mod that provides more NBT string operations.\
一个提供更多 NBT 字符串操作的模组。

## Syntax 语法

`/string <method> [target] [source...]`

The `target` and `source` parts are similar to what follows the `data modify` command.\
`target`和`source`部分与`data modify`命令后面的部分类似。

## Example 例子

- `/string isEmpty value ""` returns 1
- `/string indexOf value "asdfghjkl" value "qwerty"` returns -1
- `/string escape storage a:b c value '\'quote"slash\\'` stores `'quote\"slash\\` into `foo:bar/baz`
- `/string strip storage foo:bar baz value "  a 123   "` stores `a 123` into `foo:bar/baz`
- `/string concat2 storage a b from entity @s SelectedItem.id from entity @s Inventory[0].id`
- `/string join storage test playerUUID from value "~" from @s UUID`

## Available Methods 可用的方法

| Name                       | Source                                          | Target                               | Return Value                                                                                   |
|----------------------------|-------------------------------------------------|--------------------------------------|------------------------------------------------------------------------------------------------|
| `isBlank`                  | `String`                                        | -                                    | `1` if the input is a blank string, `0` otherwise.                                             |
| `isEmpty`                  | `String`                                        | -                                    | `1` if the input is a empty string, `0` otherwise.                                             |
| `length`                   | `String`                                        | -                                    | String length.                                                                                 |
| `toString`                 | Any NBT Element                                 | `String`                             | String length.                                                                                 |
| `toJson`                   | Any NBT Element                                 | `String`                             | String length.                                                                                 |
| `escape`                   | `String`                                        | `String`                             | String length.                                                                                 |
| `escapeNbt`                | `String`                                        | `String`                             | String length.                                                                                 |
| `escapeRegex`              | `String`                                        | `String`                             | String length.                                                                                 |
| `toLowerCase`              | `String`                                        | `String`                             | String length.                                                                                 |
| `toUpperCase`              | `String`                                        | `String`                             | String length.                                                                                 |
| `toLowerCaseInvariant`     | `String`                                        | `String`                             | String length.                                                                                 |
| `toUpperCaseInvariant`     | `String`                                        | `String`                             | String length.                                                                                 |
| `strip`                    | `String`                                        | `String`                             | String length.                                                                                 |
| `stripLeading`             | `String`                                        | `String`                             | String length.                                                                                 |
| `stripTrailing`            | `String`                                        | `String`                             | String length.                                                                                 |
| `toCharArray`              | `String`                                        | `List<String>`                       | Count of code points.                                                                          |
| `toCodePointStrings`       | `String`                                        | `List<String>`                       | Count of code points.                                                                          |
| `toCodePoints`             | `String`                                        | `IntArray`                           | Count of code points.                                                                          |
| `fromCodePoints`           | `IntArray`                                      | `String`                             | String length.                                                                                 |
| `breakCharacters`          | `String`                                        | `List<String>`                       | Count of results.                                                                              |
| `breakCharactersInvariant` | `String`                                        | `List<String>`                       | Count of results.                                                                              |
| `breakLines`               | `String`                                        | `List<String>`                       | Count of results.                                                                              |
| `breakLinesInvariant`      | `String`                                        | `List<String>`                       | Count of results.                                                                              |
| `breakSentences`           | `String`                                        | `List<String>`                       | Count of results.                                                                              |
| `breakSentencesInvariant`  | `String`                                        | `List<String>`                       | Count of results.                                                                              |
| `breakWords`               | `String`                                        | `List<String>`                       | Count of results.                                                                              |
| `breakWordsInvariant`      | `String`                                        | `List<String>`                       | Count of results.                                                                              |
| `concat`                   | `List<String>`                                  | `String`                             | String length.                                                                                 |
| `concat2`                  | `String s0, String s1, [String s2]`             | `String`                             | String length.                                                                                 |
| `trim`                     | `String src, [String trimChars]`                | `String`                             | String length.                                                                                 |
| `trimStart`                | `String src, [String trimChars]`                | `String`                             | String length.                                                                                 |
| `trimEnd`                  | `String src, [String trimChars]`                | `String`                             | String length.                                                                                 |
| `at`                       | `String src, Int index`                         | `String`                             | Unicode code point.                                                                            |
| `codePointAt`              | `String src, Int index`                         | `String`                             | Unicode code point.                                                                            |
| `codePointBefore`          | `String src, Int index`                         | `String`                             | Unicode code point.                                                                            |
| `repeat`                   | `String src, Int count`                         | `String`                             | String length.                                                                                 |
| `matchesAll`               | `String src, String pattern`                    | `List<Compound(Int start, Int end)>` | Count of match results.                                                                        |
| `matchesAllFully`          | `String src, String pattern`                    | `List<Compound(Int start, Int end)>` | Count of match results.                                                                        |
| `join`                     | `String delimiter, List elements`               | `String`                             | String length.                                                                                 |
| `substring`                | `String src, Int begin, [Int end]`              | `String`                             | String length.                                                                                 |
| `substring2`               | `String src, Int begin, [Int length]`           | `String`                             | String length.                                                                                 |
| `split`                    | `String src, String separator, [Int limit]`     | `List<String>`                       | Count of splitted string parts                                                                 |
| `indexOf`                  | `String src, String substring, [Int fromIndex]` | `String`                             | The index of the first occurrence of the substring, or `-1` if the substring is not contained. |
| `lastIndexOf`              | `String src, String substring, [Int fromIndex]` | `String`                             | The index of the last occurrence of the substring, or `-1` if the substring is not contained.  |
| `startsWith`               | `String src, String prefix, [Int offset]`       | -                                    | `1` if the second input is a prefix of the first input, `0` otherwise.                         |
| `endsWith`                 | `String src, String suffix`                     | -                                    | `1` if the second input is a suffix of the first input, `0` otherwise.                         |
| `contains`                 | `String src, String substring`                  | -                                    | `1` if the first input contains the second input, `0` otherwise.                               |
| `matches`                  | `String src, String regex`                      | -                                    | `1` if the first input matches the second input, `0` otherwise.                                |
| `replace`                  | `String src, String target, String replacement` | `String`                             | String length.                                                                                 |
| `replaceAll`               | `String src, String regex, String replacement`  | `String`                             | String length.                                                                                 |
| `replaceFirst`             | `String src, String regex, String replacement`  | `String`                             | String length.                                                                                 |

Note: Some methods actually accept any NBT element, but convert it to a string before processing.\
注：有些方法实际上接受任何 NBT 元素，但在处理之前将其转换为字符串。