package gitinternals

interface Operation {
   fun perform()

   fun concatenate(): String
}