package bloop.io

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileSystems, FileVisitResult, FileVisitor, Files, Path}
import java.nio.file.{Paths => NioPaths}

import io.github.soc.directories.ProjectDirectories

object Paths {
  private val projectDirectories = ProjectDirectories.fromProjectName("bloop")
  private def createDirFor(filepath: String): AbsolutePath =
    AbsolutePath(Files.createDirectories(NioPaths.get(filepath)))

  final val bloopCacheDir: AbsolutePath = createDirFor(projectDirectories.projectCacheDir)
  final val bloopDataDir: AbsolutePath = createDirFor(projectDirectories.projectDataDir)
  final val bloopConfigDir: AbsolutePath = createDirFor(projectDirectories.projectConfigDir)

  def getCacheDirectory(dirName: String): AbsolutePath = {
    val dir = bloopCacheDir.resolve(dirName)
    val dirPath = dir.underlying
    if (!Files.exists(dirPath)) Files.createDirectory(dirPath)
    else require(Files.isDirectory(dirPath), s"File '${dir.syntax}' is not a directory.")
    dir
  }

  def getAll(base: AbsolutePath, pattern: String): Array[AbsolutePath] = {
    val out = collection.mutable.ArrayBuffer.empty[AbsolutePath]
    val matcher = FileSystems.getDefault.getPathMatcher(pattern)
    val visitor = new FileVisitor[Path] {
      override def preVisitDirectory(directory: Path,
                                     attributes: BasicFileAttributes): FileVisitResult =
        FileVisitResult.CONTINUE

      override def postVisitDirectory(directory: Path, exception: IOException): FileVisitResult =
        FileVisitResult.CONTINUE

      override def visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult = {
        if (matcher.matches(file)) out += AbsolutePath(file)
        FileVisitResult.CONTINUE
      }

      override def visitFileFailed(file: Path, exception: IOException): FileVisitResult =
        FileVisitResult.CONTINUE
    }
    Files.walkFileTree(base.underlying, visitor)
    out.toArray
  }
}
