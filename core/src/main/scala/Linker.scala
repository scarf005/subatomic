package com.indoorvivants.subatomic

class Linker(content: Vector[(SitePath, _)], base: SitePath) {
  private val mp = content.map(_._1).toSet;

  def rooted(f: (SiteRoot with SitePath => SitePath)): String = {
    val rawLocation = f(SiteRoot)
    if (mp(rawLocation)) rawLocation.prepend(base).toString
    else
      throw new IllegalArgumentException(
        s"Could not resolve $rawLocation location in the site"
      )

  }

  def root = base.toString()
}
