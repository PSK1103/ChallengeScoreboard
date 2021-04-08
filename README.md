# ChallengeScoreboard

Simple extension of minecraft's scoreboard.

Syntax: `/challenge new <criterion> (timed <time>) <display name>` <br>
`<criterion>` is a namespaced statistic as described in [https://minecraft.fandom.com/wiki/Statistics](https://minecraft.fandom.com/wiki/Statistics). You may skip the `minecraft` namespace and the text `custom` in custom statistic.<br>
`timed <time>` is optional: it can be used to set up a timed challenge.
`time` has to be in format of combination of xxY (years), xxM (months), xxW (weeks), xxD (days), xxh (hours), xxm (minutes), xxs (seconds), where x is a number.<br>
`display name` name can be a sequence of words with format codes<br>

### Examples:
`/challenge new used:snowball &d&l&nSnowball Fight`: Create a new challenge for maximum number of snowballs thrown.<br>
`/challenge new raid_win timed 1W Pillaged!`: Create a new timed challenge for winning the maximum number of raids that runs for a week.