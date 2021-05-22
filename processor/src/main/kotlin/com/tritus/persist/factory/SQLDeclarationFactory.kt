package com.tritus.persist.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.tritus.persist.model.PersistentDataDefinition
import java.io.File

internal object SQLDeclarationFactory {
    fun create(codeGenerator: CodeGenerator, definition: PersistentDataDefinition) {
        val fileName = definition.containingFile.fileName.replace(".kt", ".sq")
        val pathInSource = definition.packageName.replace(".", "/")
        val sourceFolder = definition.containingFile.filePath.replace(pathInSource, "").split("/").dropLast(3).joinToString("/")
        val folder = File("$sourceFolder/sqldelight/$pathInSource")
        folder.mkdirs()
        val file = File(folder, fileName)
        file.writeBytes(
            """
CREATE TABLE hockeyPlayer (
  player_number INTEGER NOT NULL,
  full_name TEXT NOT NULL
);

CREATE INDEX hockeyPlayer_full_name ON hockeyPlayer(full_name);

INSERT INTO hockeyPlayer (player_number, full_name)
VALUES (15, 'Ryan Getzlaf');

selectAll:
SELECT *
FROM hockeyPlayer;

insert:
INSERT INTO hockeyPlayer(player_number, full_name)
VALUES (?, ?);

insertFullPlayerObject:
INSERT INTO hockeyPlayer(player_number, full_name)
VALUES ?;
        """.trimIndent().toByteArray()
        )
    }
}
