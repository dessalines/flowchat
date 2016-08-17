/* tslint:disable:no-unused-variable */

import { By }           from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import {
  beforeEach, beforeEachProviders,
  describe, xdescribe,
  expect, it, xit,
  async, inject
} from '@angular/core/testing';

import { CommunityCardComponent } from './community-card.component';

describe('Component: CommunityCard', () => {
  it('should create an instance', () => {
    let component = new CommunityCardComponent();
    expect(component).toBeTruthy();
  });
});
